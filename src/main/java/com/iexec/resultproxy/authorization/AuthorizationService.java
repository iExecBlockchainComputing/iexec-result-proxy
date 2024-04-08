/*
 * Copyright 2024-2024 IEXEC BLOCKCHAIN TECH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iexec.resultproxy.authorization;

import com.iexec.common.result.ResultModel;
import com.iexec.commons.poco.chain.ChainDeal;
import com.iexec.commons.poco.chain.ChainTask;
import com.iexec.commons.poco.chain.WorkerpoolAuthorization;
import com.iexec.commons.poco.security.Signature;
import com.iexec.commons.poco.tee.TeeUtils;
import com.iexec.commons.poco.utils.BytesUtils;
import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.commons.poco.utils.SignatureUtils;
import com.iexec.resultproxy.chain.IexecHubService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

import static com.iexec.resultproxy.authorization.AuthorizationError.*;

@Slf4j
@Service
public class AuthorizationService {

    private final AuthorizationRepository authorizationRepository;
    private final IexecHubService iexecHubService;

    public AuthorizationService(AuthorizationRepository authorizationRepository, IexecHubService iexecHubService) {
        this.authorizationRepository = authorizationRepository;
        this.iexecHubService = iexecHubService;
    }

    /**
     * Checks whether this execution is authorized.
     * <p>
     * The following conditions have to be verified:
     * <ul>
     * <li>The {@code WorkerpoolAuthorization} must not be null and must contain a task ID
     * <li>The task must be retrieved from the blockchain network
     * <li>The current timestamp must be before the task final deadline
     * <li>The deal must be retrieved from the blockchain network
     * <li>If the {@code WorkerpoolAuthorization} contains an enclave challenge, the on-chain deal must have a correct tag
     * <li>The {@code WorkerpoolAuthorization} has been signed with the private key of the workerpool owner found in the deal
     * </ul>
     *
     * @param workerpoolAuthorization The authorization to check
     * @return the reason if unauthorized, an empty {@code Optional} otherwise
     */
    public Optional<AuthorizationError> isAuthorizedOnExecutionWithDetailedIssue(WorkerpoolAuthorization workerpoolAuthorization) {
        if (workerpoolAuthorization == null || StringUtils.isEmpty(workerpoolAuthorization.getChainTaskId())) {
            log.error("Not authorized with empty params");
            return Optional.of(EMPTY_PARAMS_UNAUTHORIZED);
        }

        final String chainTaskId = workerpoolAuthorization.getChainTaskId();
        final ChainTask chainTask = iexecHubService.getChainTask(chainTaskId).orElse(null);
        if (chainTask == null) {
            log.error("Could not get chainTask [chainTaskId:{}]", chainTaskId);
            return Optional.of(GET_CHAIN_TASK_FAILED);
        }

        final long deadline = chainTask.getFinalDeadline();
        if (Instant.now().isAfter(Instant.ofEpochMilli(deadline))) {
            log.error("Task deadline reached [chainTaskId:{}, deadline:{}]",
                    chainTaskId, Instant.ofEpochMilli(deadline));
            return Optional.of(TASK_FINAL_DEADLINE_REACHED);
        }

        final String chainDealId = chainTask.getDealid();
        final ChainDeal chainDeal = iexecHubService.getChainDeal(chainDealId).orElse(null);
        if (chainDeal == null) {
            log.error("isAuthorizedOnExecution failed (getChainDeal failed) [chainTaskId:{}]", chainTaskId);
            return Optional.of(GET_CHAIN_DEAL_FAILED);
        }

        final boolean isTeeTask = !workerpoolAuthorization.getEnclaveChallenge().equals(BytesUtils.EMPTY_ADDRESS);
        final boolean isTeeTaskOnchain = TeeUtils.isTeeTag(chainDeal.getTag());
        if (isTeeTask != isTeeTaskOnchain) {
            log.error("Could not match on-chain task type [isTeeTask:{}, isTeeTaskOnchain:{}, chainTaskId:{}, walletAddress:{}]",
                    isTeeTask, isTeeTaskOnchain, chainTaskId, workerpoolAuthorization.getWorkerWallet());
            return Optional.of(NO_MATCH_ONCHAIN_TYPE);
        }

        final String workerpoolAddress = chainDeal.getPoolOwner();
        final boolean isSignedByWorkerpool = isSignedByHimself(workerpoolAuthorization.getHash(),
                workerpoolAuthorization.getSignature().getValue(), workerpoolAddress);

        if (!isSignedByWorkerpool) {
            log.error("isAuthorizedOnExecution failed (invalid signature) [chainTaskId:{}, isSignedByWorkerpool:{}]",
                    chainTaskId, isSignedByWorkerpool);
            return Optional.of(INVALID_SIGNATURE);
        }

        return Optional.empty();
    }

    public boolean isSignedByHimself(String message, String signature, String address) {
        return SignatureUtils.isSignatureValid(BytesUtils.stringToBytes(message), new Signature(signature), address);
    }

    public String getChallengeForWorker(WorkerpoolAuthorization workerpoolAuthorization) {
        return HashUtils.concatenateAndHash(
                workerpoolAuthorization.getWorkerWallet(),
                workerpoolAuthorization.getChainTaskId(),
                workerpoolAuthorization.getEnclaveChallenge());
    }

    // region workerpool authorization cache
    public boolean checkEnclaveSignature(ResultModel model, String walletAddress) {
        if (ResultModel.EMPTY_WEB3_SIG.equals(model.getEnclaveSignature())) {
            log.warn("Empty enclave signature {}", walletAddress);
            return false;
        }
        final String chainTaskId = model.getChainTaskId();
        final String resultHash = HashUtils.concatenateAndHash(chainTaskId, model.getDeterministHash());
        final String resultSeal = HashUtils.concatenateAndHash(walletAddress, chainTaskId, model.getDeterministHash());
        final String messageHash = HashUtils.concatenateAndHash(resultHash, resultSeal);
        final Authorization workerpoolAuthorization = authorizationRepository
                .findByChainTaskIdAndWorkerWallet(chainTaskId, walletAddress)
                .orElse(null);
        if (workerpoolAuthorization == null) {
            log.warn("No workerpool authorization was found [chainTaskId:{}, walletAddress:{}]",
                    chainTaskId, walletAddress);
            return false;
        }
        final String enclaveChallenge = workerpoolAuthorization.getEnclaveChallenge();
        boolean isSignedByEnclave = isSignedByHimself(messageHash, model.getEnclaveSignature(), enclaveChallenge);
        if (isSignedByEnclave) {
            log.info("Valid enclave signature received, allowed to push result");
            authorizationRepository.deleteById(workerpoolAuthorization.getId());
            log.debug("Workerpool authorization entry removed [chainTaskId:{}, workerWallet:{}]",
                    workerpoolAuthorization.getChainTaskId(), workerpoolAuthorization.getWorkerWallet());
        } else {
            log.warn("Invalid enclave signature [chainTaskId:{}, walletAddress:{}]", chainTaskId, walletAddress);
        }
        return isSignedByEnclave;
    }

    public void putIfAbsent(WorkerpoolAuthorization workerpoolAuthorization) {
        try {
            authorizationRepository.save(new Authorization(workerpoolAuthorization));
            log.debug("Workerpool authorization entry added [chainTaskId:{}, workerWallet:{}]",
                    workerpoolAuthorization.getChainTaskId(), workerpoolAuthorization.getWorkerWallet());
        } catch (DataAccessException e) {
            log.warn("Workerpool authorization entry not added [chainTaskId:{}, workerWallet: {}]",
                    workerpoolAuthorization.getChainTaskId(), workerpoolAuthorization.getWorkerWallet(), e);
        }
    }
    // endregion

}

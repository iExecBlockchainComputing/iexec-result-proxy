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

import com.iexec.commons.poco.chain.ChainDeal;
import com.iexec.commons.poco.chain.ChainTask;
import com.iexec.commons.poco.chain.ChainTaskStatus;
import com.iexec.commons.poco.chain.WorkerpoolAuthorization;
import com.iexec.commons.poco.security.Signature;
import com.iexec.commons.poco.tee.TeeUtils;
import com.iexec.commons.poco.utils.BytesUtils;
import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.commons.poco.utils.SignatureUtils;
import com.iexec.resultproxy.chain.IexecHubService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.iexec.resultproxy.authorization.AuthorizationError.*;

@Slf4j
@Service
public class AuthorizationService {

    private final IexecHubService iexecHubService;

    public AuthorizationService(IexecHubService iexecHubService) {
        this.iexecHubService = iexecHubService;
    }

    /**
     * Checks whether this execution is authorized.
     * If not authorized, return the reason.
     * Otherwise, returns an empty {@link Optional}.
     */
    public Optional<AuthorizationError> isAuthorizedOnExecutionWithDetailedIssue(WorkerpoolAuthorization workerpoolAuthorization, boolean isTeeTask) {
        if (workerpoolAuthorization == null || StringUtils.isEmpty(workerpoolAuthorization.getChainTaskId())) {
            log.error("Not authorized with empty params");
            return Optional.of(EMPTY_PARAMS_UNAUTHORIZED);
        }

        final String chainTaskId = workerpoolAuthorization.getChainTaskId();
        Optional<ChainTask> optionalChainTask = iexecHubService.getChainTask(chainTaskId);
        if (optionalChainTask.isEmpty()) {
            log.error("Could not get chainTask [chainTaskId:{}]", chainTaskId);
            return Optional.of(GET_CHAIN_TASK_FAILED);
        }
        final ChainTask chainTask = optionalChainTask.get();

        final ChainTaskStatus taskStatus = chainTask.getStatus();
        if (taskStatus != ChainTaskStatus.ACTIVE) {
            log.error("Task not active onchain [chainTaskId:{}, status:{}]",
                    chainTaskId, taskStatus);
            return Optional.of(TASK_NOT_ACTIVE);
        }

        final String chainDealId = chainTask.getDealid();
        Optional<ChainDeal> optionalChainDeal = iexecHubService.getChainDeal(chainDealId);
        if (optionalChainDeal.isEmpty()) {
            log.error("isAuthorizedOnExecution failed (getChainDeal failed) [chainTaskId:{}]", chainTaskId);
            return Optional.of(GET_CHAIN_DEAL_FAILED);
        }
        ChainDeal chainDeal = optionalChainDeal.get();

        boolean isTeeTaskOnchain = TeeUtils.isTeeTag(chainDeal.getTag());
        if (isTeeTask != isTeeTaskOnchain) {
            log.error("Could not match onchain task type [isTeeTask:{}, isTeeTaskOnchain:{}, chainTaskId:{}, walletAddress:{}]",
                    isTeeTask, isTeeTaskOnchain, chainTaskId, workerpoolAuthorization.getWorkerWallet());
            return Optional.of(NO_MATCH_ONCHAIN_TYPE);
        }

        String workerpoolAddress = chainDeal.getPoolOwner();
        boolean isSignedByWorkerpool = isSignedByHimself(workerpoolAuthorization.getHash(),
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
}

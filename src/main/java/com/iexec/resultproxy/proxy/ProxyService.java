/*
 * Copyright 2020-2024 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy.proxy;

import com.iexec.common.result.ComputedFile;
import com.iexec.common.result.ResultModel;
import com.iexec.common.utils.FileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.commons.poco.chain.ChainContribution;
import com.iexec.commons.poco.chain.ChainDeal;
import com.iexec.commons.poco.chain.ChainTask;
import com.iexec.commons.poco.chain.ChainTaskStatus;
import com.iexec.commons.poco.tee.TeeUtils;
import com.iexec.resultproxy.authorization.AuthorizationService;
import com.iexec.resultproxy.chain.IexecHubService;
import com.iexec.resultproxy.ipfs.IpfsResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.iexec.common.utils.IexecFileHelper.SLASH_IEXEC_OUT;
import static com.iexec.common.utils.IexecFileHelper.readComputedFile;
import static com.iexec.commons.poco.chain.ChainContributionStatus.REVEALED;

/**
 * Service class to manage all the results. If the result is public, it will be stored on IPFS. If there is a dedicated
 * beneficiary, the result will be pushed to mongo.
 */
@Service
@Slf4j
public class ProxyService {

    private final AuthorizationService authorizationService;
    private final IexecHubService iexecHubService;
    private final IpfsResultService ipfsResultService;

    public ProxyService(AuthorizationService authorizationService,
                        IexecHubService iexecHubService,
                        IpfsResultService ipfsResultService) {
        this.authorizationService = authorizationService;
        this.iexecHubService = iexecHubService;
        this.ipfsResultService = ipfsResultService;
    }

    boolean canUploadResult(ResultModel model, String walletAddress) {
        final String chainTaskId = model.getChainTaskId();
        final byte[] zip = model.getZip();

        // check if result has been already uploaded
        if (isResultFound(chainTaskId)) {
            log.error("Trying to upload result that has been already uploaded [chainTaskId:{}, uploadRequester:{}]",
                    chainTaskId, walletAddress);
            return false;
        }

        // TODO [PoCo Boost] on-chain deal id available in result model to avoid fetching task
        final ChainTask chainTask = iexecHubService.getChainTask(chainTaskId).orElse(null);
        if (chainTask == null) {
            log.error("Trying to upload result for TEE but getChainTask failed [chainTaskId:{}, uploader:{}]",
                    chainTaskId, walletAddress);
            return false;
        }

        final ChainDeal chainDeal = iexecHubService.getChainDeal(chainTask.getDealid()).orElse(null);
        if (chainDeal == null) {
            log.error("Trying to upload result for TEE but getChainDeal failed [chainTaskId:{}, uploader:{}]",
                    chainTaskId, walletAddress);
            return false;
        }

        final boolean isTeeTask = TeeUtils.isTeeTag(chainDeal.getTag());

        // Standard tasks
        if (!isTeeTask) {
            return isResultValid(chainTaskId, walletAddress, zip);
        }

        // TODO remove this case in the future. As we support 2 stack versions, it will be a major after deprecated proxy controller endpoints removal
        // TEE tasks with token containing the requester address
        if (chainDeal.getRequester().equalsIgnoreCase(walletAddress)) {
            return chainTask.getStatus() == ChainTaskStatus.ACTIVE;
        }

        // TEE tasks with ResultModel containing the enclave signature
        return authorizationService.checkEnclaveSignature(model, walletAddress);
    }

    /**
     * A result for a standard task is considered as valid if:
     * <ul>
     * <li>It has an associated on-chain contribution
     * <li>The associated on-chain contribution status is {@code REVEALED}
     * <li>The on-chain contribution result hash is the one we compute here again
     * </ul>
     *
     * @param chainTaskId   ID of the task
     * @param walletAddress Address of the uploader
     * @param zip           Result as a zip
     * @return {@literal true} if the result is valid, {@literal false} otherwise.
     */
    boolean isResultValid(String chainTaskId, String walletAddress, byte[] zip) {
        final String resultFolderPath = getResultFolderPath(chainTaskId);
        final String resultZipPath = resultFolderPath + ".zip";
        final String zipDestinationPath = resultFolderPath + SLASH_IEXEC_OUT;
        try {
            final ChainContribution chainContribution = iexecHubService.getChainContribution(chainTaskId, walletAddress)
                    .orElse(ChainContribution.builder().build());
            // ContributionStatus of chainTask should be REVEALED
            if (chainContribution.getStatus() != REVEALED) {
                log.error("Trying to upload result even though ChainContributionStatus is not REVEALED" +
                                " [chainTaskId:{}, uploadRequester:{}, status:{}]",
                        chainTaskId, walletAddress, chainContribution.getStatus());
                return false;
            }

            final String onChainHash = chainContribution.getResultHash();
            try {
                Files.write(Path.of(resultZipPath), zip);
            } catch (IOException e) {
                log.error("Can't write result file [chainTaskId:{}, uploader:{}]", chainTaskId, walletAddress);
                return false;
            }
            FileHelper.unZipFile(resultZipPath, zipDestinationPath);

            final ComputedFile computedFile = readComputedFile(chainTaskId, zipDestinationPath);
            final String resultDigest = ResultUtils.computeWeb2ResultDigest(computedFile, resultFolderPath);
            final String computedResultHash = ResultUtils.computeResultHash(chainTaskId, resultDigest);

            if (!Objects.equals(computedResultHash, onChainHash)) {
                log.error("Trying to upload result but on-chain result hash differs from given hash " +
                                "[chainTaskId:{}, uploader:{}, onChainHash:{}, computedResultHash:{}]",
                        chainTaskId, walletAddress, onChainHash, computedResultHash);
                return false;
            }

            return true;
        } finally {
            FileHelper.deleteFolder(resultFolderPath);
        }
    }

    String getResultFolderPath(String chainTaskId) {
        return "/tmp/" + chainTaskId;
    }

    boolean isResultFound(String chainTaskId) {
        return ipfsResultService.doesResultExist(chainTaskId);
    }

    String addResult(ResultModel model) {
        return ipfsResultService.addResult(model.getChainTaskId(), model.getZip());
    }
}

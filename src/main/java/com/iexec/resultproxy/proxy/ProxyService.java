/*
 * Copyright 2020-2023 IEXEC BLOCKCHAIN TECH
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
import com.iexec.common.utils.FileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.commons.poco.chain.ChainContribution;
import com.iexec.commons.poco.chain.ChainContributionStatus;
import com.iexec.commons.poco.chain.ChainTask;
import com.iexec.commons.poco.chain.ChainTaskStatus;
import com.iexec.commons.poco.task.TaskDescription;
import com.iexec.resultproxy.chain.IexecHubService;
import com.iexec.resultproxy.ipfs.IpfsResultService;
import com.iexec.resultproxy.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static com.iexec.common.utils.IexecFileHelper.SLASH_IEXEC_OUT;
import static com.iexec.common.utils.IexecFileHelper.readComputedFile;

/**
 * Service class to manage all the results. If the result is public, it will be stored on IPFS. If there is a dedicated
 * beneficiary, the result will be pushed to mongo.
 */
@Service
@Slf4j
public class ProxyService {

    private final IexecHubService iexecHubService;
    private final IpfsResultService ipfsResultService;

    public ProxyService(IexecHubService iexecHubService,
                        IpfsResultService ipfsResultService) {
        this.iexecHubService = iexecHubService;
        this.ipfsResultService = ipfsResultService;
    }


    boolean canUploadResult(String chainTaskId, String walletAddress, byte[] zip) {
        if (iexecHubService.isTeeTask(chainTaskId)){
            Optional<ChainTask> chainTask = iexecHubService.getChainTask(chainTaskId);//TODO Add requester field to getChainTask
            if (chainTask.isEmpty()){
                log.error("Trying to upload result for TEE but getChainTask failed [chainTaskId:{}, uploader:{}]",
                        chainTaskId, walletAddress);
                return false;
            }
            boolean isActive = chainTask.get().getStatus().equals(ChainTaskStatus.ACTIVE);

            Optional<TaskDescription> taskDescription = iexecHubService.getTaskDescriptionFromChain(chainTaskId);
            if (taskDescription.isEmpty()){
                log.error("Trying to upload result for TEE but getTaskDescription failed [chainTaskId:{}, uploader:{}]",
                        chainTaskId, walletAddress);
                return false;
            }
            boolean isRequesterCredentials = taskDescription.get().getRequester().equalsIgnoreCase(walletAddress);

            return isActive && isRequesterCredentials;
        } else {
            // check if result has been already uploaded
            if (isResultFound(chainTaskId)) {
                log.error("Trying to upload result that has been already uploaded [chainTaskId:{}, uploadRequester:{}]",
                        chainTaskId, walletAddress);
                return false;
            }

            // ContributionStatus of chainTask should be REVEALED
            boolean isChainContributionStatusSetToRevealed = iexecHubService.isStatusTrueOnChain(chainTaskId,
                    walletAddress, ChainContributionStatus.REVEALED);
            if (!isChainContributionStatusSetToRevealed) {
                log.error("Trying to upload result even though ChainContributionStatus is not REVEALED [chainTaskId:{}, uploadRequester:{}]",
                        chainTaskId, walletAddress);
                return false;
            }

            return isResultValid(chainTaskId, walletAddress, zip);
        }
    }

    /**
     * A result is considered as valid if:
     * <ul>
     * <li>It has an associated on-chain contribution
     * <li>The on-chain contribution result hash is the one we compute here again.
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
            final Optional<ChainContribution> oChainContribution = iexecHubService.getChainContribution(chainTaskId, walletAddress);
            if (oChainContribution.isEmpty()) {
                log.error("Trying to upload result but no on-chain contribution [chainTaskId:{}, uploader:{}]",
                        chainTaskId, walletAddress);
                return false;
            }

            final String onChainHash = oChainContribution.get().getResultHash();
            try {
                Files.write(Path.of(resultZipPath), zip);
            } catch (IOException e) {
                log.error("Can't write result file [chainTaskId:{}, walletAddress:{}]", chainTaskId, walletAddress);
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

    String addResult(Result result, byte[] data) {
        if (result == null || result.getChainTaskId() == null) {
            return "";
        }
        return ipfsResultService.addResult(result, data);
    }

    Optional<byte[]> getResult(String chainTaskId) {
        return ipfsResultService.getResult(chainTaskId);
    }
}

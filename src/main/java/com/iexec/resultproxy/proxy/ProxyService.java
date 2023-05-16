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


import com.iexec.commons.poco.chain.ChainContributionStatus;
import com.iexec.commons.poco.chain.ChainTask;
import com.iexec.commons.poco.chain.ChainTaskStatus;
import com.iexec.commons.poco.task.TaskDescription;
import com.iexec.resultproxy.chain.IexecHubService;
import com.iexec.resultproxy.ipfs.IpfsResultService;
import com.iexec.resultproxy.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

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


    boolean canUploadResult(String chainTaskId, String walletAddress) {
        if (iexecHubService.isTeeTask(chainTaskId)){
            Optional<ChainTask> chainTask = iexecHubService.getChainTask(chainTaskId);//TODO Add requester field to getChainTask
            if (chainTask.isEmpty()){
                log.error("Trying to upload result for TEE but getChainTask failed [chainTaskId:{}, uploader:{}]",
                        chainTaskId, walletAddress);
                return false;
            }
            boolean isActive = chainTask.get().getStatus().equals(ChainTaskStatus.ACTIVE);

            TaskDescription taskDescription = iexecHubService.getTaskDescription(chainTaskId);
            if (taskDescription == null){
                log.error("Trying to upload result for TEE but getTaskDescription failed [chainTaskId:{}, uploader:{}]",
                        chainTaskId, walletAddress);
                return false;
            }
            boolean isRequesterCredentials = taskDescription.getRequester().equalsIgnoreCase(walletAddress);

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

            return true;
        }
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

package com.iexec.resultproxy.proxy;

import java.io.IOException;
import java.util.Optional;

import com.iexec.common.chain.ChainContributionStatus;
import com.iexec.common.chain.ChainTask;
import com.iexec.common.chain.ChainTaskStatus;
import com.iexec.common.task.TaskDescription;
import com.iexec.resultproxy.chain.IexecHubService;
import com.iexec.resultproxy.ipfs.IpfsResultService;
import com.iexec.resultproxy.result.Result;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * Service class to manage all the results. If the result is public, it will be stored on IPFS. If there is a dedicated
 * beneficiary, the result will be pushed to mongo.
 */
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

    Optional<byte[]> getResult(String chainTaskId) throws IOException {
        return ipfsResultService.getResult(chainTaskId);
    }
}

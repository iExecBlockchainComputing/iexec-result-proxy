package com.iexec.resultproxy.ipfs;

import java.util.Optional;

import com.iexec.resultproxy.ipfs.task.IpfsNameService;
import com.iexec.resultproxy.result.AbstractResultStorage;
import com.iexec.resultproxy.result.Result;

import org.springframework.stereotype.Service;

@Service
public class IpfsResultService extends AbstractResultStorage {


    private static final String IPFS_ADDRESS_PREFIX = "/ipfs/";

    private IpfsService ipfsService;
    private IpfsNameService ipfsNameService;


    public IpfsResultService(IpfsService ipfsService,
                             IpfsNameService ipfsNameService) {
        this.ipfsService = ipfsService;
        this.ipfsNameService = ipfsNameService;
    }


    @Override
    public String addResult(Result result, byte[] data) {
        String taskId = result.getChainTaskId();
        String existingIpfsHash = ipfsNameService.getIpfsHashForTask(taskId);
        if (!existingIpfsHash.isEmpty()) {
            return "";
        }
        String resultFileName = getResultFilename(taskId);
        String ipfsHash = ipfsService.add(resultFileName, data);
        ipfsNameService.setIpfsHashForTask(taskId, ipfsHash);
        return IPFS_ADDRESS_PREFIX + ipfsHash;
    }

    @Override
    public Optional<byte[]> getResult(String chainTaskId) {
        String ipfsHash = ipfsNameService.getIpfsHashForTask(chainTaskId);
        if (!ipfsHash.isEmpty()) {
            return ipfsService.get(ipfsHash);
        }
        return Optional.empty();
    }

}

package com.iexec.resultproxy.ipfs;

import com.iexec.resultproxy.ipfs.task.IpfsName;
import com.iexec.resultproxy.ipfs.task.IpfsNameService;
import com.iexec.resultproxy.proxy.AbstractResultRepo;
import com.iexec.resultproxy.proxy.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class IpfsResultService extends AbstractResultRepo {


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

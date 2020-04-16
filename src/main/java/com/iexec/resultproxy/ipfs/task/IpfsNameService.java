package com.iexec.resultproxy.ipfs.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/*
 * This service hold metadata for iExec result pushed to IPFS
 * It allows converting a taskId to an ipfsHash
 *
 * /!\ WARN: Mongo volume should be kept between reboots to keep taskId->ipfsHash mapping alive
 * */
@Service
@Slf4j
public class IpfsNameService {


    private IpfsNameRepository ipfsNameRepository;

    public IpfsNameService(IpfsNameRepository ipfsNameRepository) {
        this.ipfsNameRepository = ipfsNameRepository;
    }

    public void setIpfsHashForTask(String taskId, String ipfsHash) {
        if (!getIpfsHashForTask(taskId).isEmpty()) {
            log.error("Can't setIpfsHashForTask (ipfsHash already set for task result) [taskId:{}, existingIpfsHash:{}]", taskId, ipfsHash);
            return;
        }
        ipfsNameRepository.save(new IpfsName(taskId, ipfsHash));
    }

    public String getIpfsHashForTask(String taskId) {
        Optional<IpfsName> taskResultIpfsHash = ipfsNameRepository.findByTaskId(taskId);
        if (taskResultIpfsHash.isPresent()) {
            return taskResultIpfsHash.get().getIpfsHash();
        }
        return "";
    }


}

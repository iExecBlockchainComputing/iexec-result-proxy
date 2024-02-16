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

package com.iexec.resultproxy.ipfs;

import com.iexec.resultproxy.ipfs.task.IpfsNameService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IpfsResultService {

    private static final String IPFS_ADDRESS_PREFIX = "/ipfs/";
    private static final String IPFS_FILENAME_PREFIX = "iexec-result-";

    private final IpfsService ipfsService;
    private final IpfsNameService ipfsNameService;


    public IpfsResultService(IpfsService ipfsService,
                             IpfsNameService ipfsNameService) {
        this.ipfsService = ipfsService;
        this.ipfsNameService = ipfsNameService;
    }

    public String addResult(String taskId, byte[] data) {
        String existingIpfsHash = ipfsNameService.getIpfsHashForTask(taskId);
        if (!existingIpfsHash.isEmpty()) {
            return "";
        }
        String resultFileName = getResultFilename(taskId);
        String ipfsHash = ipfsService.add(resultFileName, data);
        ipfsNameService.setIpfsHashForTask(taskId, ipfsHash);
        return IPFS_ADDRESS_PREFIX + ipfsHash;
    }

    public boolean doesResultExist(String chainTaskId) {
        return getResult(chainTaskId).isPresent();
    }

    public Optional<byte[]> getResult(String chainTaskId) {
        String ipfsHash = ipfsNameService.getIpfsHashForTask(chainTaskId);
        if (!ipfsHash.isEmpty()) {
            return ipfsService.get(ipfsHash);
        }
        return Optional.empty();
    }

    private String getResultFilename(String hash) {
        return IPFS_FILENAME_PREFIX + hash;
    }

}

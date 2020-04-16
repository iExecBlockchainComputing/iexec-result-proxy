package com.iexec.resultproxy.ipfs.task;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@NoArgsConstructor
public class IpfsName {

    @Id
    private String id;

    @Version
    private Long version;

    private String taskId;
    private String ipfsHash;

    public IpfsName(String taskId, String ipfsHash) {
        this.taskId = taskId;
        this.ipfsHash = ipfsHash;
    }
}

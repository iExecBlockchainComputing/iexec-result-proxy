package com.iexec.resultproxy.ipfs.task;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IpfsNameRepository extends MongoRepository<IpfsName, String> {

    Optional<IpfsName> findByTaskId(String taskId);

}

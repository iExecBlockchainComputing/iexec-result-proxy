package com.iexec.resultproxy.mongo;

import com.iexec.resultproxy.proxy.Result;
import com.iexec.resultproxy.proxy.AbstractResultStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
@Slf4j
public class MongoResultService extends AbstractResultStorage {

    private final GridFsOperations gridOperations;

    public MongoResultService(GridFsOperations gridOperations) {
        this.gridOperations = gridOperations;
    }

    @Override
    public String addResult(Result result, byte[] data) {
        if (result == null || result.getChainTaskId() == null || result.getChainTaskId().isEmpty()) {
            return "";
        }
        InputStream inputStream = new ByteArrayInputStream(data);
        String resultFileName = getResultFilename(result.getChainTaskId());
        gridOperations.store(inputStream, resultFileName, result);
        // return resultRepositoryConfig.getResultRepositoryURL() + "/results/" + result.getChainTaskId();
        return "/results/" + result.getChainTaskId();
    }

    @Override
    public Optional<byte[]> getResult(String chainTaskId) {
        String resultFileName = getResultFilename(chainTaskId);
        GridFsResource[] resources = gridOperations.getResources(resultFileName);
        if (resources.length == 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(org.apache.commons.io.IOUtils.toByteArray(resources[0].getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to getResult [chainTaskId:{}]", chainTaskId);
        }
        return Optional.empty();
    }

    @Override
    public boolean doesResultExist(String chainTaskId) {
        Query query = Query.query(Criteria.where("filename").is(getResultFilename(chainTaskId)));
        return gridOperations.findOne(query) != null;
    }
}

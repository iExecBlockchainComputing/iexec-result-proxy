package com.iexec.resultproxy.chain;

import com.iexec.common.chain.IexecHubAbstractService;

import org.springframework.stereotype.Service;

@Service
public class IexecHubService extends IexecHubAbstractService {

    public IexecHubService(CredentialsService credentialsService, Web3jService web3jService, ChainConfig chainConfig) {
        super(credentialsService.getCredentials(), web3jService, chainConfig.getHubAddress());
    }
}

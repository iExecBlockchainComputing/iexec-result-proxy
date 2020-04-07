package com.iexec.resultproxy.chain;

import com.iexec.common.chain.Web3jAbstractService;

import org.springframework.stereotype.Service;

@Service
public class Web3jService extends Web3jAbstractService {

    public Web3jService(ChainConfig chainConfig) {
        super(chainConfig.getPrivateChainAddress(), chainConfig.getGasPriceMultiplier(), chainConfig.getGasPriceCap(),
                chainConfig.isSidechain());
    }

}
package com.iexec.resultproxy.chain;

import static com.iexec.common.chain.ChainContributionStatus.CONTRIBUTED;
import static com.iexec.common.chain.ChainContributionStatus.REVEALED;

import java.util.Optional;

import com.iexec.common.chain.ChainContribution;
import com.iexec.common.chain.ChainContributionStatus;
import com.iexec.common.chain.IexecHubAbstractService;

import org.springframework.stereotype.Service;


@Service
public class IexecHubService extends IexecHubAbstractService {

    public IexecHubService(CredentialsService credentialsService, Web3jService web3jService, ChainConfig chainConfig) {
        super(credentialsService.getCredentials(), web3jService, chainConfig.getHubAddress());
    }

    public boolean isStatusTrueOnChain(String chainTaskId, String walletAddress, ChainContributionStatus wishedStatus) {
        Optional<ChainContribution> optional = getChainContribution(chainTaskId, walletAddress);
        if (!optional.isPresent()) {
            return false;
        }

        ChainContribution chainContribution = optional.get();
        ChainContributionStatus chainStatus = chainContribution.getStatus();
        switch (wishedStatus) {
            case CONTRIBUTED:
                // has at least contributed
                return chainStatus.equals(CONTRIBUTED) || chainStatus.equals(REVEALED);
            case REVEALED:
                // has at least revealed
                return chainStatus.equals(REVEALED);
            default:
                return false;
        }
    }
}

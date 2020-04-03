package com.iexec.resultproxy.chain;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CredentialsService {

    private Credentials credentials;

    public CredentialsService() {
        try {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            credentials = Credentials.create(ecKeyPair);
            log.info("Loaded new wallet credentials [walletAddress:{}] ", credentials.getAddress());
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            log.error("Cannot load wallet credentials");
            e.printStackTrace();
        }
    }

    public Credentials getCredentials() {
        return credentials;
    }
}

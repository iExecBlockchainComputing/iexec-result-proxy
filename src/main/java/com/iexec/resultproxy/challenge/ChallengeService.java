package com.iexec.resultproxy.challenge;

import com.iexec.common.result.eip712.Eip712Challenge;
import com.iexec.common.utils.BytesUtils;
import com.iexec.common.utils.SignatureUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ChallengeService {
    /**
     * TODO:
     * Rename AuthorizationService to JwtService
     * Rename Authorization to SignedChallenge
     */

    private Eip712ChallengeService eip712ChallengeService;

    ChallengeService(Eip712ChallengeService challengeService) {
        this.eip712ChallengeService = challengeService;
    }

    public Eip712Challenge createChallenge(Integer chainId) {
        return eip712ChallengeService.generateEip712Challenge(chainId);
    }

    public void invalidateChallenge(String eip712ChallengeString) {
        eip712ChallengeService.invalidateEip712ChallengeString(eip712ChallengeString);
    }

    public SignedChallenge tokenToSignedChallengeObject(String token) {
        if ((token == null) || (token.split("_").length != 3)) {
            return null;
        }

        String[] parts = token.split("_");
        return SignedChallenge.builder()
                .challenge(parts[0])
                .challengeSignature(parts[1])
                .walletAddress(parts[2])
                .build();
    }

    public boolean isSignedChallengeValid(SignedChallenge authorization) {
        if (authorization == null) {
            log.error("Authorization should not be null [authorization:{}]", authorization);
            return false;
        }

        String eip712ChallengeString = authorization.getChallenge();
        String challengeSignature = authorization.getChallengeSignature();
        String walletAddress = authorization.getWalletAddress();

        challengeSignature = Numeric.cleanHexPrefix(challengeSignature);

        if (challengeSignature.length() < 130) {
            log.error("Eip712ChallengeString has a bad signature format [downloadRequester:{}]", walletAddress);
            return false;
        }
        String r = challengeSignature.substring(0, 64);
        String s = challengeSignature.substring(64, 128);
        String v = challengeSignature.substring(128, 130);

        //ONE: check if eip712Challenge is in eip712Challenge map
        if (!eip712ChallengeService.containsEip712ChallengeString(eip712ChallengeString)) {
            log.error("Eip712ChallengeString provided doesn't match any challenge [downloadRequester:{}]", walletAddress);
            return false;
        }

        //TWO: check if ecrecover on eip712Challenge & signature match address
        if (!SignatureUtils.doesSignatureMatchesAddress(BytesUtils.stringToBytes(r), BytesUtils.stringToBytes(s),
                eip712ChallengeString, StringUtils.lowerCase(walletAddress))) {
            log.error("Signature provided doesn't match walletAddress [downloadRequester:{}, sign.r:{}, sign.s:{}, eip712ChallengeString:{}]",
                    walletAddress, r, s, eip712ChallengeString);
            return false;
        }

        return true;
    }

}

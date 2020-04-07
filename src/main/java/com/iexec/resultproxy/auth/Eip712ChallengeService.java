package com.iexec.resultproxy.auth;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.iexec.common.result.eip712.Eip712Challenge;
import com.iexec.common.result.eip712.Eip712ChallengeUtils;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

@Service
public class Eip712ChallengeService {

    private int challengeId;
    private ExpiringMap<Integer, String> challengeMap;

    Eip712ChallengeService() {
        this.challengeMap = ExpiringMap.builder()
                .expiration(60, TimeUnit.MINUTES)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .build();
        challengeId = 0;
    }

    private static String generateRandomToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.encodeBase64URLSafeString(token);
    }

    Eip712Challenge generateEip712Challenge(Integer chainId) {
        Eip712Challenge eip712Challenge = new Eip712Challenge(generateRandomToken(), chainId);
        this.saveEip712ChallengeString(Eip712ChallengeUtils.getEip712ChallengeString(eip712Challenge));
        return eip712Challenge;
    }

    private void saveEip712ChallengeString(String eip712ChallengeString) {
        challengeId++;
        challengeMap.put(challengeId, eip712ChallengeString);
    }

    boolean containsEip712ChallengeString(String eip712ChallengeString) {
        return challengeMap.containsValue(eip712ChallengeString);
    }

    void invalidateEip712ChallengeString(String eip712ChallengeString) {
        challengeMap.entrySet().removeIf(entry -> entry.getValue().equals(eip712ChallengeString));
    }

}

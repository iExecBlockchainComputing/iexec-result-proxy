package com.iexec.resultproxy.challenge;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.iexec.common.result.eip712.Eip712Challenge;
import com.iexec.common.result.eip712.Eip712ChallengeUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

@Service
@Slf4j
public class Eip712ChallengeService {

    /**
     * Contains all valid challenges.
     * They expire after a certain amount of time.
     * Once a challenge has been used, it becomes invalid.
     * <br>
     * This could technically be a simple Collection of {@code eip712Challenge},
     * but there's currently no out-of-the-box Collection with expiration settings.
     */
    private final ExpiringMap<String, String> challenges;

    Eip712ChallengeService() {
        this.challenges = ExpiringMap.builder()
                .expiration(60, TimeUnit.MINUTES)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .build();
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

    /**
     * This operation is thread-safe.
     */
    private void saveEip712ChallengeString(String eip712ChallengeString) {
        challenges.put(eip712ChallengeString, eip712ChallengeString);
    }

    /**
     * This operation is thread-safe.
     */
    boolean containsEip712ChallengeString(String eip712ChallengeString) {
        return challenges.containsKey(eip712ChallengeString);
    }

    /**
     * This operation is thread-safe.
     */
    void invalidateEip712ChallengeString(String eip712ChallengeString) {
        challenges.remove(eip712ChallengeString);
    }

}

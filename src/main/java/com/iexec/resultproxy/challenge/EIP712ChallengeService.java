package com.iexec.resultproxy.challenge;

import com.iexec.commons.poco.eip712.EIP712Domain;
import com.iexec.commons.poco.eip712.entity.Challenge;
import com.iexec.commons.poco.eip712.entity.EIP712Challenge;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EIP712ChallengeService {

    /**
     * Contains all valid challenges.
     * They expire after a certain amount of time.
     * Once a challenge has been used, it becomes invalid.
     * <br>
     * This could technically be a simple Collection of {@code eip712Challenge},
     * but there's currently no out-of-the-box Collection with expiration settings.
     */
    private final ExpiringMap<String, String> challenges;
    private final SecureRandom secureRandom = new SecureRandom();

    EIP712ChallengeService() {
        this.challenges = ExpiringMap.builder()
                .expiration(60, TimeUnit.MINUTES)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .build();
    }

    private String generateRandomToken() {
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.encodeBase64URLSafeString(token);
    }

    EIP712Challenge generateEip712Challenge(Integer chainId) {
        final EIP712Domain domain = new EIP712Domain("iExec Result Repository", "1", chainId, null);
        final Challenge challenge = new Challenge(generateRandomToken());
        EIP712Challenge eip712Challenge = new EIP712Challenge(domain, challenge);
        this.saveEip712ChallengeString(eip712Challenge.getHash());
        return eip712Challenge;
    }

    /**
     * This operation is thread-safe.
     */
    private void saveEip712ChallengeString(String eip712ChallengeString) {
        challenges.put(eip712ChallengeString, null);
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

/*
 * Copyright 2022 IEXEC BLOCKCHAIN TECH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iexec.resultproxy.jwt;

import com.iexec.common.security.SignedChallenge;
import com.iexec.common.utils.ContextualLockRunner;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class JwtService {
    static final int KEY_SIZE = 128;
    private static final byte[] jwtKey = SecureRandom.getSeed(KEY_SIZE);
    private final ContextualLockRunner<String> contextualLockRunner = new ContextualLockRunner<>();
    private final JwtRepository jwtRepository;

    public JwtService(JwtRepository jwtRepository) {
        this.jwtRepository = jwtRepository;
    }

    public String getOrCreateJwt(SignedChallenge signedChallenge) {
        final String walletAddress = signedChallenge.getWalletAddress();
        // Synchronizing the following is mandatory:
        // we need to ensure there won't be 2 different JWT issued for the same wallet.
        // This is ensured by making synchronous the get-and-save operation.
        return contextualLockRunner.applyWithLock(walletAddress, this::getOrCreateJwt);
    }

    private String getOrCreateJwt(String walletAddress) {
        String jwtString;
        Optional<Jwt> oExistingJwt = findByWalletAddress(walletAddress);

        if (oExistingJwt.isPresent()) {
            jwtString = oExistingJwt.get().getJwtString(); // TODO generate new token
        } else {
            jwtString = createJwt(walletAddress);
            save(new Jwt(walletAddress, jwtString));
        }
        return jwtString;
    }

    String createJwt(String walletAddress) {
        return Jwts.builder()
                .setAudience(walletAddress)
                .setIssuedAt(new Date())
                .setSubject(RandomStringUtils.randomAlphanumeric(64))
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .compact();
    }

    public boolean isValidJwt(String jwtString) {
        try {
            String claimedWalletAddress = getWalletAddressFromJwtString(jwtString);
            Jwt existingJwt = findByWalletAddress(claimedWalletAddress).orElseThrow();
            return jwtString.equals(existingJwt.getJwtString());
        } catch (Exception e) {
            log.warn("Invalid JWT token [message:{}]", e.getMessage());
            return false;
        }
    }

    public String getWalletAddressFromJwtString(String jwtString) {
        return Jwts.parser()
                .setSigningKey(jwtKey)
                .parseClaimsJws(jwtString)
                .getBody()
                .getAudience();
    }

    private Optional<Jwt> findByWalletAddress(String walletAddress) {
        return jwtRepository.findByWalletAddress(walletAddress);
    }

    private void save(Jwt jwt) {
        jwtRepository.save(jwt);
    }
}

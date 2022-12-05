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
import com.iexec.common.utils.FileHelper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class JwtService {
    static final int KEY_SIZE = 128;
    private final byte[] jwtKey;
    private final ContextualLockRunner<String> contextualLockRunner = new ContextualLockRunner<>();
    private final JwtRepository jwtRepository;

    public JwtService(JwtConfig jwtConfig, JwtRepository jwtRepository) throws IOException {
        this.jwtRepository = jwtRepository;
        this.jwtKey = initKey(jwtConfig.getKeyPath());
    }

    public String getOrCreateJwt(SignedChallenge signedChallenge) {
        final String walletAddress = signedChallenge.getWalletAddress();
        // Synchronizing the following is mandatory:
        // we need to ensure there won't be 2 different JWT issued for the same wallet.
        // This is ensured by making synchronous the get-and-save operation.
        return contextualLockRunner.applyWithLock(walletAddress, this::getOrCreateJwt);
    }

    /**
     * Reads the key used to sign JWT tokens from a file.
     * <p>
     * If the file does not exist, it is created.
     * If the file content is not Base64 encoded, the application exits.
     * <p>
     * There does not seem to be a best practice between hosting a key within a file or a database,
     * for an easier implementation, the key is currently hosted in a file for the moment.
     *
     * @param jwtKeyPath Path to the file hosting the key.
     * @return A byte array containing the key.
     * @throws IOException if an error occurs during file system interactions
     */
    private byte[] initKey(String jwtKeyPath) throws IOException {
        Path path = Path.of(jwtKeyPath);
        if (!path.toFile().exists()) {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[KEY_SIZE];
            random.nextBytes(bytes);
            String content = Base64.getEncoder().encodeToString(bytes);
            FileHelper.createFileWithContent(jwtKeyPath, content);
        }
        String payload = Files.readString(path);
        return Base64.getDecoder().decode(payload);
    }

    /**
     * Retrieves existing JWT from the database or creates it.
     * <p>
     * A new signed token will be created in the following cases:
     * <ul>
     * <li> If a JWT is not found, an exception will be thrown and a new token will be created.
     * <li> If a JWT is found but was not signed with the correct key.
     * @param walletAddress Ethereum address for which
     * @return A valid JWT token signed with this instance key.
     */
    private String getOrCreateJwt(String walletAddress) {
        String jwtString;
        try {
            Jwt jwt = findByWalletAddress(walletAddress).orElseThrow();
            jwtString = jwt.getJwtString();
            getWalletAddressFromJwtString(jwtString);
        } catch (IllegalArgumentException | JwtException | NoSuchElementException e) {
            log.warn("Valid JWT token not found in storage, generating a new one");
            jwtString = createJwt(walletAddress);
            save(new Jwt(walletAddress, jwtString));
        }
        return jwtString;
    }

    String createJwt(String walletAddress) {
        return Jwts.builder()
                .setAudience(walletAddress)
                .setIssuedAt(new Date())
                .setSubject(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .compact();
    }

    public boolean isValidJwt(String jwtString) {
        try {
            String claimedWalletAddress = getWalletAddressFromJwtString(jwtString);
            Jwt existingJwt = findByWalletAddress(claimedWalletAddress).orElseThrow();
            return jwtString.equals(existingJwt.getJwtString());
        } catch (IllegalArgumentException | JwtException | NoSuchElementException e) {
            log.warn("Invalid JWT token [message:{}]", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the content from the 'audience' claim of a JWT token.
     * <p>
     * For a token generated by the running result-proxy instance, the 'audience' claim content
     * should be a valid Ethereum wallet address. The tokens are signed with a key and if an unsigned token or
     * a token signed with another key is provided, a runtime exception is thrown.
     *
     * @param jwtString String representation of the JWT token to be parsed
     * @return Wallet address extracted from the 'audience' claim
     */
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

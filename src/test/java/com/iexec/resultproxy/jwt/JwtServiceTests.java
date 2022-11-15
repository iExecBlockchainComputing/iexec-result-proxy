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
import com.iexec.common.utils.FileHelper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.iexec.resultproxy.jwt.JwtService.KEY_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtServiceTests {

    @TempDir
    private static Path tmpDir;
    private JwtConfig jwtConfig;
    @Mock
    private JwtRepository jwtRepository;
    private JwtService jwtService;
    private String walletAddress;
    private static final byte[] badJwtKey = SecureRandom.getSeed(KEY_SIZE);

    private String getWalletAddress() {
        try {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            return Credentials.create(ecKeyPair).getAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void init() throws IOException {
        jwtConfig = new JwtConfig(String.join(File.separator, tmpDir.toString(), ".key"));
        MockitoAnnotations.openMocks(this);
        walletAddress = getWalletAddress();
        jwtService = new JwtService(jwtConfig, jwtRepository);
    }

    @Test
    void shouldNotCreateServiceWhenKeyNotBase64Encoded() throws IOException {
        Path keyFilePath = Path.of(jwtConfig.getKeyPath());
        Files.deleteIfExists(keyFilePath);
        FileHelper.createFileWithContent(jwtConfig.getKeyPath(), UUID.randomUUID().toString());
        assertThrows(IllegalArgumentException.class,
                () -> new JwtService(jwtConfig, jwtRepository));
        Files.deleteIfExists(keyFilePath);
    }

    //region key persistence
    @Test
    void shouldValidateTokenWhenKeyFileExists() throws IOException {
        String token = jwtService.createJwt(walletAddress);
        JwtService newService = new JwtService(jwtConfig, jwtRepository);
        assertAll(
                () -> assertEquals(walletAddress, jwtService.getWalletAddressFromJwtString(token)),
                () -> assertEquals(walletAddress, newService.getWalletAddressFromJwtString(token))
        );
    }

    @Test
    void shouldNotValidateTokenWhenKeyFileRecreated() throws IOException {
        String token = jwtService.createJwt(walletAddress);
        Files.deleteIfExists(Path.of(jwtConfig.getKeyPath()));
        JwtService newService = new JwtService(jwtConfig, jwtRepository);
        assertAll(
                () -> assertEquals(walletAddress, jwtService.getWalletAddressFromJwtString(token)),
                () -> assertThrows(SignatureException.class,
                        () -> newService.getWalletAddressFromJwtString(token))
        );
    }
    //endregion

    //region createJwt and getWalletAddressFromJwtString
    @Test
    void readWalletAddressFromValidToken() {
        String token = jwtService.createJwt(walletAddress);
        String extractedToken = jwtService.getWalletAddressFromJwtString(token);
        assertAll(
                () -> verifyNoInteractions(jwtRepository),
                () -> assertEquals(walletAddress, extractedToken)
        );
    }

    @Test
    void failToReadWalletAddressOnWronglySignedToken() {
        String badToken = Jwts.builder()
                .setAudience(walletAddress)
                .setIssuedAt(new Date())
                .setSubject(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, badJwtKey)
                .compact();
        assertAll(
                () -> verifyNoInteractions(jwtRepository),
                () -> assertThrows(SignatureException.class,
                        () -> jwtService.getWalletAddressFromJwtString(badToken))
        );
    }

    @Test
    void failToReadWalletAddressOnUnsignedToken() {
        String unsignedToken = Jwts.builder()
                .setAudience(walletAddress)
                .setIssuedAt(new Date())
                .setSubject(UUID.randomUUID().toString())
                .compact();
        assertAll(
                () -> verifyNoInteractions(jwtRepository),
                () -> assertThrows(UnsupportedJwtException.class,
                        () -> jwtService.getWalletAddressFromJwtString(unsignedToken),
                "Unsigned Claims JWTs are not supported.")
        );
    }
    //endregion

    //region getOrCreateJwt
    @Test
    void createJwtIfNotPresentInRepository() {
        SignedChallenge signedChallenge = SignedChallenge.builder().walletAddress(walletAddress).build();
        when(jwtRepository.findByWalletAddress(walletAddress)).thenReturn(Optional.empty());
        String jwtToken = jwtService.getOrCreateJwt(signedChallenge);
        assertAll(
                () -> verify(jwtRepository).findByWalletAddress(walletAddress),
                () -> verify(jwtRepository).save(any()),
                () -> assertEquals(walletAddress, jwtService.getWalletAddressFromJwtString(jwtToken))
        );
    }

    @Test
    void getJwtIfPresentInRepository() {
        String token = jwtService.createJwt(walletAddress);
        SignedChallenge signedChallenge = SignedChallenge.builder().walletAddress(walletAddress).build();
        Jwt expectedJwt = new Jwt(walletAddress, token);
        when(jwtRepository.findByWalletAddress(walletAddress)).thenReturn(Optional.of(expectedJwt));
        String resultToken = jwtService.getOrCreateJwt(signedChallenge);
        assertAll(
                () -> verify(jwtRepository).findByWalletAddress(any()),
                () -> assertEquals(token, resultToken)

        );
    }
    //endregion

    //region isValidJwt
    @Test
    void validTokenIsValid() {
        String token = jwtService.createJwt(walletAddress);
        when(jwtRepository.findByWalletAddress(walletAddress)).thenReturn(Optional.of(new Jwt(walletAddress, token)));
        boolean isValid = jwtService.isValidJwt(token);
        assertAll(
                () -> verify(jwtRepository).findByWalletAddress(any()),
                () -> assertTrue(isValid)
        );
    }

    @Test
    void unsignedTokenIsNotValid() {
        String unsignedToken = Jwts.builder()
                .setAudience(walletAddress)
                .setIssuedAt(new Date())
                .setSubject(UUID.randomUUID().toString())
                .compact();
        boolean isValid = jwtService.isValidJwt(unsignedToken);
        assertAll(
                () -> verifyNoInteractions(jwtRepository),
                () -> assertFalse(isValid)
        );
    }

    @Test
    void wronglySignedTokenIsNotValid() {
        String badToken = Jwts.builder()
                .setAudience(walletAddress)
                .setIssuedAt(new Date())
                .setSubject(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, badJwtKey)
                .compact();
        boolean isValid = jwtService.isValidJwt(badToken);
        assertAll (
                () -> verifyNoInteractions(jwtRepository),
                () -> assertFalse(isValid)
        );
    }

    @Test
    void tokenIsNotValidWhenNotInRepository() {
        String token = jwtService.createJwt(walletAddress);
        when(jwtRepository.findByWalletAddress(walletAddress)).thenReturn(Optional.empty());
        boolean isValid = jwtService.isValidJwt(token);
        assertAll(
                () -> verify(jwtRepository).findByWalletAddress(any()),
                () -> assertFalse(isValid)
        );
    }

    @Test
    void tokenIsNotValidWhenTamperedRepository() {
        String forgedToken = jwtService.createJwt(getWalletAddress());
        when(jwtRepository.findByWalletAddress(walletAddress)).thenReturn(Optional.of(new Jwt(walletAddress, forgedToken)));
        String queryToken = jwtService.createJwt(walletAddress);
        boolean isValid = jwtService.isValidJwt(queryToken);
        assertAll(
                () -> verify(jwtRepository).findByWalletAddress(any()),
                () -> assertFalse(isValid)
        );
    }
    //endregion

}

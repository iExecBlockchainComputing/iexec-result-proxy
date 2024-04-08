/*
 * Copyright 2024-2024 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy.authorization;

import com.iexec.common.result.ResultModel;
import com.iexec.commons.poco.chain.ChainDeal;
import com.iexec.commons.poco.chain.ChainTask;
import com.iexec.commons.poco.chain.WorkerpoolAuthorization;
import com.iexec.commons.poco.security.Signature;
import com.iexec.commons.poco.utils.BytesUtils;
import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.commons.poco.utils.SignatureUtils;
import com.iexec.resultproxy.chain.IexecHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.iexec.commons.poco.chain.ChainTaskStatus.ACTIVE;
import static com.iexec.commons.poco.utils.SignatureUtils.signMessageHashAndGetSignature;
import static com.iexec.resultproxy.TestUtils.*;
import static com.iexec.resultproxy.authorization.AuthorizationError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataMongoTest
@Testcontainers
class AuthorizationServiceTests {

    private static final String RESULT_DIGEST = "0x3210";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse(System.getProperty("mongo.image")));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
    }

    @Autowired
    private AuthorizationRepository authorizationRepository;
    @Mock
    private IexecHubService iexecHubService;

    private AuthorizationService authorizationService;

    private Credentials enclaveCreds;
    private Credentials workerCreds;

    @BeforeEach
    void beforeEach() throws GeneralSecurityException {
        MockitoAnnotations.openMocks(this);
        enclaveCreds = Credentials.create(Keys.createEcKeyPair());
        workerCreds = Credentials.create(Keys.createEcKeyPair());
        authorizationRepository.deleteAll();
        authorizationService = new AuthorizationService(authorizationRepository, iexecHubService);
    }

    // region isAuthorizedOnExecutionWithDetailedIssue
    @Test
    void shouldBeAuthorizedOnExecutionOfTeeTaskWithDetails() {
        final ChainDeal chainDeal = getChainDeal();
        final ChainTask chainTask = getChainTask(ACTIVE);
        final WorkerpoolAuthorization auth = getWorkerpoolAuthorization(true);
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(chainTask.getDealid())).thenReturn(Optional.of(chainDeal));

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth).isEmpty();
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWithNullAuthorizationWithDetails() {
        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(null);
        assertThat(isAuth).isEqualTo(Optional.of(EMPTY_PARAMS_UNAUTHORIZED));
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWithEmptyAuthorizationWithDetails() {
        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(WorkerpoolAuthorization.builder().build());
        assertThat(isAuth).isEqualTo(Optional.of(EMPTY_PARAMS_UNAUTHORIZED));
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenTaskTypeNotMatchedOnchainWithDetails() {
        final ChainDeal chainDeal = getChainDeal();
        final ChainTask chainTask = getChainTask(ACTIVE);
        final WorkerpoolAuthorization auth = WorkerpoolAuthorization.builder()
                .chainTaskId("0x1111111111111111111111111111111111111111111111111111111111111111")
                .workerWallet("0x87ae2b87b5db23830572988fb1f51242fbc471ce")
                .enclaveChallenge(BytesUtils.EMPTY_ADDRESS)
                .build();
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(chainTask.getDealid())).thenReturn(Optional.of(chainDeal));

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth).isEqualTo(Optional.of(NO_MATCH_ONCHAIN_TYPE));
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenGetTaskFailedWithDetails() {
        final WorkerpoolAuthorization auth = getWorkerpoolAuthorization(true);
        when(iexecHubService.isTeeTask(auth.getChainTaskId())).thenReturn(true);
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.empty());

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth).isEqualTo(Optional.of(GET_CHAIN_TASK_FAILED));
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenFinalDeadlineReached() {
        final WorkerpoolAuthorization auth = getWorkerpoolAuthorization(true);
        final ChainTask chainTask = ChainTask.builder()
                .dealid(CHAIN_DEAL_ID)
                .finalDeadline(Instant.now().minus(5L, ChronoUnit.SECONDS).toEpochMilli())
                .build();
        when(iexecHubService.isTeeTask(auth.getChainTaskId())).thenReturn(true);
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth).isEqualTo(Optional.of(TASK_FINAL_DEADLINE_REACHED));
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenGetDealFailedWithDetails() {
        final ChainTask chainTask = getChainTask(ACTIVE);
        final WorkerpoolAuthorization auth = getWorkerpoolAuthorization(true);
        auth.setSignature(new Signature(POOL_WRONG_SIGNATURE));

        when(iexecHubService.isTeeTask(auth.getChainTaskId())).thenReturn(true);
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(chainTask.getDealid())).thenReturn(Optional.empty());

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth).isEqualTo(Optional.of(GET_CHAIN_DEAL_FAILED));
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenPoolSignatureIsNotValidWithDetails() {
        final ChainDeal chainDeal = getChainDeal();
        final ChainTask chainTask = getChainTask(ACTIVE);
        final WorkerpoolAuthorization auth = getWorkerpoolAuthorization(true);
        auth.setSignature(new Signature(POOL_WRONG_SIGNATURE));

        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(chainTask.getDealid())).thenReturn(Optional.of(chainDeal));

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth).isEqualTo(Optional.of(INVALID_SIGNATURE));
    }
    // endregion

    @Test
    void checkIsSignedByHimself() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        final ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        final String privateKey = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
        final String workerWallet = Credentials.create(ecKeyPair).getAddress();
        final WorkerpoolAuthorization authorization = WorkerpoolAuthorization.builder()
                .chainTaskId("0x1234")
                .enclaveChallenge("0x5678")
                .workerWallet(workerWallet)
                .build();
        final String challenge = authorizationService.getChallengeForWorker(authorization);
        final String signedChallenge = signMessageHashAndGetSignature(challenge, privateKey).getValue();
        assertThat(authorizationService.isSignedByHimself(challenge, signedChallenge, workerWallet)).isTrue();
        assertThat(authorizationService.isSignedByHimself(challenge, signedChallenge, Keys.toChecksumAddress(workerWallet))).isTrue();
    }

    @Test
    void getChallengeForWorker() {
        final WorkerpoolAuthorization authorization = WorkerpoolAuthorization.builder()
                .chainTaskId("0x0123")
                .enclaveChallenge("0x4567")
                .workerWallet("0xabcd")
                .build();
        final String challenge = authorizationService.getChallengeForWorker(authorization);
        assertThat(challenge).isEqualTo("0x0a17b60a69e733c4199912dc3c5bfd4b17aa6bcfbf3cfbfe6230f00e21f96b85");
    }

    // region workerpool authorization cache
    @Test
    void shouldNotBeSignedByEnclaveWhenEnclaveSignatureIsEmpty() {
        final WorkerpoolAuthorization authorization = getWorkerpoolAuthorization(true);
        authorizationService.putIfAbsent(authorization);
        final ResultModel model = ResultModel.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .enclaveSignature(ResultModel.EMPTY_WEB3_SIG)
                .deterministHash(RESULT_DIGEST)
                .build();
        assertThat(authorizationService.checkEnclaveSignature(model, workerCreds.getAddress())).isFalse();
    }

    @Test
    void shouldNotBeSignedByEnclaveWhenWorkerpoolAuthorizationNotExist() {
        final ResultModel model = ResultModel.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .enclaveSignature(getEnclaveSignature(enclaveCreds.getEcKeyPair()))
                .deterministHash(RESULT_DIGEST)
                .build();
        assertThat(authorizationService.checkEnclaveSignature(model, workerCreds.getAddress())).isFalse();
    }

    @Test
    void shouldNotBeSignedByEnclaveWhenSignedByOther() throws GeneralSecurityException {
        final WorkerpoolAuthorization authorization = getWorkerpoolAuthorization(true);
        authorizationService.putIfAbsent(authorization);
        final ResultModel model = ResultModel.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .enclaveSignature(getEnclaveSignature(Keys.createEcKeyPair()))
                .deterministHash(RESULT_DIGEST)
                .build();
        assertThat(authorizationService.checkEnclaveSignature(model, workerCreds.getAddress())).isFalse();
    }

    @Test
    void shouldBeSignedByEnclave() {
        final WorkerpoolAuthorization authorization = getWorkerpoolAuthorization(true);
        authorizationService.putIfAbsent(authorization);
        final ResultModel model = ResultModel.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .enclaveSignature(getEnclaveSignature(enclaveCreds.getEcKeyPair()))
                .deterministHash(RESULT_DIGEST)
                .build();
        assertThat(authorizationService.checkEnclaveSignature(model, workerCreds.getAddress())).isTrue();
    }
    // endregion

    // region putIfAbsent
    @Test
    void shouldNotAddAuthorizationTwiceInCollection() {
        final WorkerpoolAuthorization stdAuthorization = getWorkerpoolAuthorization(false);
        authorizationService.putIfAbsent(stdAuthorization);
        assertThat(authorizationRepository.count()).isOne();
        authorizationService.putIfAbsent(stdAuthorization);
        assertThat(authorizationRepository.count()).isOne();
        authorizationRepository.deleteAll();
        final WorkerpoolAuthorization teeAuthorization = getWorkerpoolAuthorization(true);
        authorizationService.putIfAbsent(teeAuthorization);
        assertThat(authorizationRepository.count()).isOne();
        authorizationService.putIfAbsent(teeAuthorization);
        assertThat(authorizationRepository.count()).isOne();
    }
    // endregion

    // region utils
    String getEnclaveSignature(ECKeyPair ecKeyPair) {
        final String resultHash = HashUtils.concatenateAndHash(CHAIN_TASK_ID, RESULT_DIGEST);
        final String resultSeal = HashUtils.concatenateAndHash(workerCreds.getAddress(), CHAIN_TASK_ID, RESULT_DIGEST);
        final String messageHash = HashUtils.concatenateAndHash(resultHash, resultSeal);
        return SignatureUtils.signMessageHashAndGetSignature(messageHash,
                Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey())).getValue();
    }

    WorkerpoolAuthorization getWorkerpoolAuthorization(boolean isTeeTask) {
        final String enclaveChallenge = isTeeTask ? enclaveCreds.getAddress() : BytesUtils.EMPTY_ADDRESS;
        final String hash = HashUtils.concatenateAndHash(workerCreds.getAddress(), CHAIN_TASK_ID, enclaveChallenge);
        final Signature signature = SignatureUtils.signMessageHashAndGetSignature(hash, POOL_PRIVATE);
        return WorkerpoolAuthorization.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .enclaveChallenge(enclaveChallenge)
                .workerWallet(workerCreds.getAddress())
                .signature(signature)
                .build();
    }
    // endregion
}

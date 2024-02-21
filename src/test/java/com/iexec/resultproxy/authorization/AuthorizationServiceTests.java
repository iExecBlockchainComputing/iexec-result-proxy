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
import com.iexec.commons.poco.tee.TeeUtils;
import com.iexec.commons.poco.utils.BytesUtils;
import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.commons.poco.utils.SignatureUtils;
import com.iexec.commons.poco.utils.TestUtils;
import com.iexec.resultproxy.chain.IexecHubService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Optional;

import static com.iexec.commons.poco.chain.ChainTaskStatus.ACTIVE;
import static com.iexec.commons.poco.chain.ChainTaskStatus.UNSET;
import static com.iexec.commons.poco.utils.SignatureUtils.signMessageHashAndGetSignature;
import static com.iexec.resultproxy.authorization.AuthorizationError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthorizationServiceTests {

    private static final String CHAIN_TASK_ID = "0x0123";
    private static final String RESULT_DIGEST = "0x3210";
    private static final String WALLET_ADDRESS = "0xabcd";

    @Mock
    IexecHubService iexecHubService;

    @InjectMocks
    private AuthorizationService authorizationService;

    private Credentials enclaveCreds;

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
        enclaveCreds = Credentials.create(Keys.createEcKeyPair());
    }

    // region isAuthorizedOnExecutionWithDetailedIssue
    @Test
    void shouldBeAuthorizedOnExecutionOfTeeTaskWithDetails() {
        ChainDeal chainDeal = getChainDeal();
        ChainTask chainTask = TestUtils.getChainTask(ACTIVE);
        WorkerpoolAuthorization auth = TestUtils.getTeeWorkerpoolAuth();
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(chainTask.getDealid())).thenReturn(Optional.of(chainDeal));

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth).isEmpty();
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWithNullAuthorizationWithDetails() {
        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(null);
        assertThat(isAuth)
                .isNotEmpty()
                .contains(EMPTY_PARAMS_UNAUTHORIZED);
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWithEmptyAuthorizationWithDetails() {
        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(WorkerpoolAuthorization.builder().build());
        assertThat(isAuth).isNotEmpty()
                .contains(EMPTY_PARAMS_UNAUTHORIZED);
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenTaskTypeNotMatchedOnchainWithDetails() {
        ChainDeal chainDeal = getChainDeal();
        ChainTask chainTask = TestUtils.getChainTask(ACTIVE);
        WorkerpoolAuthorization auth = WorkerpoolAuthorization.builder()
                .chainTaskId("0x1111111111111111111111111111111111111111111111111111111111111111")
                .workerWallet("0x87ae2b87b5db23830572988fb1f51242fbc471ce")
                .enclaveChallenge(BytesUtils.EMPTY_HEX_STRING_32)
                .build();
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(chainTask.getDealid())).thenReturn(Optional.of(chainDeal));

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth)
                .isNotEmpty()
                .contains(NO_MATCH_ONCHAIN_TYPE);
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenGetTaskFailedWithDetails() {
        WorkerpoolAuthorization auth = TestUtils.getTeeWorkerpoolAuth();
        when(iexecHubService.isTeeTask(auth.getChainTaskId())).thenReturn(true);
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.empty());

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth)
                .isNotEmpty()
                .contains(GET_CHAIN_TASK_FAILED);
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenTaskNotActiveWithDetails() {
        WorkerpoolAuthorization auth = TestUtils.getTeeWorkerpoolAuth();
        ChainTask chainTask = TestUtils.getChainTask(UNSET);
        when(iexecHubService.isTeeTask(auth.getChainTaskId())).thenReturn(true);
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth)
                .isNotEmpty()
                .contains(TASK_NOT_ACTIVE);
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenGetDealFailedWithDetails() {
        ChainTask chainTask = TestUtils.getChainTask(ACTIVE);
        WorkerpoolAuthorization auth = TestUtils.getTeeWorkerpoolAuth();
        auth.setSignature(new Signature(TestUtils.POOL_WRONG_SIGNATURE));

        when(iexecHubService.isTeeTask(auth.getChainTaskId())).thenReturn(true);
        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(chainTask.getDealid())).thenReturn(Optional.empty());

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth)
                .isNotEmpty()
                .contains(GET_CHAIN_DEAL_FAILED);
    }

    @Test
    void shouldNotBeAuthorizedOnExecutionOfTeeTaskWhenPoolSignatureIsNotValidWithDetails() {
        ChainDeal chainDeal = getChainDeal();
        ChainTask chainTask = TestUtils.getChainTask(ACTIVE);
        WorkerpoolAuthorization auth = TestUtils.getTeeWorkerpoolAuth();
        auth.setSignature(new Signature(TestUtils.POOL_WRONG_SIGNATURE));

        when(iexecHubService.getChainTask(auth.getChainTaskId())).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(chainTask.getDealid())).thenReturn(Optional.of(chainDeal));

        Optional<AuthorizationError> isAuth = authorizationService.isAuthorizedOnExecutionWithDetailedIssue(auth);
        assertThat(isAuth)
                .isNotEmpty()
                .contains(INVALID_SIGNATURE);
    }
    // endregion

    @Test
    void checkIsSignedByHimself() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        String privateKey = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
        String workerWallet = Credentials.create(ecKeyPair).getAddress();
        WorkerpoolAuthorization authorization = WorkerpoolAuthorization.builder()
                .chainTaskId("0x1234")
                .enclaveChallenge("0x5678")
                .workerWallet(workerWallet)
                .build();
        String challenge = authorizationService.getChallengeForWorker(authorization);
        String signedChallenge = signMessageHashAndGetSignature(challenge, privateKey).getValue();
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
    void shouldNotBeSignedByEnclave() {
        final WorkerpoolAuthorization authorization = getWorkerpoolAuthorization();
        authorizationService.putIfAbsent(authorization);
        final ResultModel model = ResultModel.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .enclaveSignature(Numeric.toHexString(new byte[65]))
                .deterministHash(RESULT_DIGEST)
                .build();
        assertThat(authorizationService.checkEnclaveSignature(model, WALLET_ADDRESS)).isFalse();
    }

    @Test
    void shouldBeSignedByEnclave() {
        final WorkerpoolAuthorization authorization = getWorkerpoolAuthorization();
        authorizationService.putIfAbsent(authorization);
        final String resultHash = HashUtils.concatenateAndHash(CHAIN_TASK_ID, RESULT_DIGEST);
        final String resultSeal = HashUtils.concatenateAndHash(WALLET_ADDRESS, CHAIN_TASK_ID, RESULT_DIGEST);
        final String messageHash = HashUtils.concatenateAndHash(resultHash, resultSeal);
        final String enclaveSignature = SignatureUtils.signMessageHashAndGetSignature(messageHash,
                Numeric.toHexStringWithPrefix(enclaveCreds.getEcKeyPair().getPrivateKey())).getValue();
        final ResultModel model = ResultModel.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .enclaveSignature(enclaveSignature)
                .deterministHash(RESULT_DIGEST)
                .build();
        assertThat(authorizationService.checkEnclaveSignature(model, WALLET_ADDRESS)).isTrue();
    }
    // endregion

    // region utils
    ChainDeal getChainDeal() {
        return ChainDeal.builder()
                .poolOwner("0xc911f9345717ba7c8ec862ce002af3e058df84e4")
                .tag(TeeUtils.TEE_SCONE_ONLY_TAG)
                .build();
    }

    WorkerpoolAuthorization getWorkerpoolAuthorization() {
        return WorkerpoolAuthorization.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .enclaveChallenge(enclaveCreds.getAddress())
                .workerWallet(WALLET_ADDRESS)
                .build();
    }
    // endregion
}

/*
 * Copyright 2024-2025 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy.proxy;

import com.iexec.common.result.ResultModel;
import com.iexec.commons.poco.chain.WorkerpoolAuthorization;
import com.iexec.resultproxy.authorization.AuthorizationService;
import com.iexec.resultproxy.challenge.EIP712ChallengeService;
import com.iexec.resultproxy.ipfs.task.IpfsNameService;
import com.iexec.resultproxy.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static com.iexec.resultproxy.authorization.AuthorizationError.EMPTY_PARAMS_UNAUTHORIZED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProxyControllerTests {

    private static final String CHAIN_TASK_ID = "0x1";
    private static final String WORKER_WALLET = "0x2";
    private static final String ENCLAVE_CHALLENGE = "0x3";
    // Worker challenge computed by proxy to verify WORKER authorization
    private static final String WORKER_CHALLENGE = "0x4";
    // Signed worker challenge provided by worker
    private static final String WORKER_AUTH = "0x5";
    private static final String TOKEN = "TOKEN";

    // workerpool authorization generated by scheduler and provided by worker
    private static final WorkerpoolAuthorization AUTHORIZATION = WorkerpoolAuthorization.builder()
            .chainTaskId(CHAIN_TASK_ID)
            .workerWallet(WORKER_WALLET)
            .enclaveChallenge(ENCLAVE_CHALLENGE)
            .build();


    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private EIP712ChallengeService challengeService;
    @Mock
    private IpfsNameService ipfsNameService;
    @Mock
    private JwtService jwtService;
    @Mock
    private ProxyService proxyService;

    @Spy
    @InjectMocks
    private ProxyController controller;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // region deprecated methods, to remove in v10
    @Test
    void shouldCallAddResult() {
        final ResultModel model = ResultModel.builder().build();
        controller.addResultDeprecated(TOKEN, model);
        verify(controller).addResult(TOKEN, model);
    }

    @Test
    void shouldCallIsResultUploaded() {
        controller.isResultUploadedDeprecated(CHAIN_TASK_ID, TOKEN);
        verify(controller).isResultUploaded(CHAIN_TASK_ID, TOKEN);
    }

    @Test
    void shouldCallGetIpfsHashForTask() {
        when(ipfsNameService.getIpfsHashForTask(CHAIN_TASK_ID)).thenReturn("");
        controller.getIpfsHashForTaskDeprecated(CHAIN_TASK_ID);
        verify(controller).getIpfsHashForTask(CHAIN_TASK_ID);
    }
    // endregion

    // region getJwt
    @Test
    void shouldNotGetJwtWhenAuthorizationIsNotValid() {
        when(authorizationService.getChallengeForWorker(AUTHORIZATION)).thenReturn(WORKER_CHALLENGE);
        when(authorizationService.isSignedByHimself(WORKER_CHALLENGE, WORKER_AUTH, WORKER_WALLET)).thenReturn(false);
        assertThat(controller.getJwt(WORKER_AUTH, AUTHORIZATION))
                .isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    void shouldNotGetJwtWhenWorkerpoolAuthorizationIsNotValid() {
        when(authorizationService.getChallengeForWorker(AUTHORIZATION)).thenReturn(WORKER_CHALLENGE);
        when(authorizationService.isSignedByHimself(WORKER_CHALLENGE, WORKER_AUTH, WORKER_WALLET)).thenReturn(true);
        when(authorizationService.isAuthorizedOnExecutionWithDetailedIssue(AUTHORIZATION)).thenReturn(Optional.of(EMPTY_PARAMS_UNAUTHORIZED));
        assertThat(controller.getJwt(WORKER_AUTH, AUTHORIZATION))
                .isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    void shouldGetJwtWhenAuthorizationIsValid() {
        when(authorizationService.getChallengeForWorker(AUTHORIZATION)).thenReturn(WORKER_CHALLENGE);
        when(authorizationService.isSignedByHimself(WORKER_CHALLENGE, WORKER_AUTH, WORKER_WALLET)).thenReturn(true);
        when(authorizationService.isAuthorizedOnExecutionWithDetailedIssue(AUTHORIZATION)).thenReturn(Optional.empty());
        when(jwtService.getOrCreateJwt(WORKER_WALLET)).thenReturn(TOKEN);
        assertThat(controller.getJwt(WORKER_AUTH, AUTHORIZATION))
                .isEqualTo(ResponseEntity.ok(TOKEN));
    }
    // endregion

    // region addResult
    @Test
    void shouldNotAddResultWhenJwtNotValid() {
        when(jwtService.isValidJwt("token")).thenReturn(false);
        assertThat(controller.addResult("token", ResultModel.builder().build()))
                .isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build());
    }

    @Test
    void shouldNotAddResultOnChecksFailure() {
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.canUploadResult(any(), any())).thenReturn(false);
        assertThat(controller.addResult("token", ResultModel.builder().build()))
                .isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build());
    }

    @Test
    void shouldNotAddResultOnEmptyResultLink() {
        final ResultModel model = ResultModel.builder().build();
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.canUploadResult(any(), any())).thenReturn(true);
        when(proxyService.addResult(any())).thenReturn("");
        assertThat(controller.addResult("token", model))
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build());
    }

    @Test
    void shouldAddResult() {
        final ResultModel model = ResultModel.builder().build();
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.canUploadResult(any(), any())).thenReturn(true);
        when(proxyService.addResult(any())).thenReturn("/ipfs");
        assertThat(controller.addResult("token", model))
                .isEqualTo(ResponseEntity.ok("/ipfs"));
    }
    // endregion

    // region isResultUploaded
    @Test
    void shouldAnswerUnauthorizedWhenJwtNotValid() {
        when(jwtService.isValidJwt("token")).thenReturn(false);
        assertThat(controller.isResultUploaded("chainTaskId", "token"))
                .isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    void shouldAnswerNotFoundWhenResultNotFound() {
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.isResultFound("chainTaskId")).thenReturn(false);
        assertThat(controller.isResultUploaded("chainTaskId", "token"))
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Test
    void shouldAnswerNoContentWhenResultFound() {
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.isResultFound("chainTaskId")).thenReturn(true);
        assertThat(controller.isResultUploaded("chainTaskId", "token"))
                .isEqualTo(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }
    // endregion

    // region getIpfsHashForTask
    @Test
    void shouldAnswerNotFoundWhenIpfsHashNotFound() {
        when(ipfsNameService.getIpfsHashForTask(CHAIN_TASK_ID)).thenReturn("");
        assertThat(controller.getIpfsHashForTask(CHAIN_TASK_ID))
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build());
    }
    // endregion
}

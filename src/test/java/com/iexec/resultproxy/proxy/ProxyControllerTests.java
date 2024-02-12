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

package com.iexec.resultproxy.proxy;

import com.iexec.common.result.ResultModel;
import com.iexec.resultproxy.authorization.AuthorizationService;
import com.iexec.resultproxy.challenge.EIP712ChallengeService;
import com.iexec.resultproxy.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProxyControllerTests {
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private EIP712ChallengeService challengeService;
    @Mock
    private JwtService jwtService;
    @Mock
    private ProxyService proxyService;

    @InjectMocks
    private ProxyController controller;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

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
        when(proxyService.canUploadResult(any(), any(), any())).thenReturn(false);
        assertThat(controller.addResult("token", ResultModel.builder().build()))
                .isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build());
    }

    @Test
    void shouldNotAddResultOnEmptyResultLink() {
        final ResultModel model = ResultModel.builder().build();
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.canUploadResult(any(), any(), any())).thenReturn(true);
        when(proxyService.addResult(any(), any())).thenReturn("");
        assertThat(controller.addResult("token", model))
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build());
    }

    @Test
    void shouldAddResult() {
        final ResultModel model = ResultModel.builder().build();
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.canUploadResult(any(), any(), any())).thenReturn(true);
        when(proxyService.addResult(any(), any())).thenReturn("/ipfs");
        assertThat(controller.addResult("token", model))
                .isEqualTo(ResponseEntity.ok("/ipfs"));
    }
    // endregion

    // region isResultUploaded
    @Test
    void shouldNotAnswerWhenJwtNotValid() {
        when(jwtService.isValidJwt("token")).thenReturn(false);
        assertThat(controller.isResultUploaded("chainTaskId", "token"))
                .isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    void shouldAnswerNotFound() {
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.isResultFound("chainTaskId")).thenReturn(false);
        assertThat(controller.isResultUploaded("chainTaskId", "token"))
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Test
    void shouldAnswerNoContent() {
        when(jwtService.isValidJwt("token")).thenReturn(true);
        when(proxyService.isResultFound("chainTaskId")).thenReturn(true);
        assertThat(controller.isResultUploaded("chainTaskId", "token"))
                .isEqualTo(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }
    // endregion
}

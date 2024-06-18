/*
 * Copyright 2020-2024 IEXEC BLOCKCHAIN TECH
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
import com.iexec.commons.poco.chain.ChainContribution;
import com.iexec.commons.poco.chain.ChainDeal;
import com.iexec.commons.poco.chain.ChainTask;
import com.iexec.commons.poco.chain.ChainTaskStatus;
import com.iexec.commons.poco.tee.TeeUtils;
import com.iexec.commons.poco.utils.BytesUtils;
import com.iexec.resultproxy.authorization.AuthorizationService;
import com.iexec.resultproxy.chain.IexecHubService;
import com.iexec.resultproxy.ipfs.IpfsResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.File;
import java.util.Base64;
import java.util.Optional;

import static com.iexec.commons.poco.chain.ChainContributionStatus.CONTRIBUTED;
import static com.iexec.commons.poco.chain.ChainContributionStatus.REVEALED;
import static com.iexec.commons.poco.chain.ChainTaskStatus.ACTIVE;
import static com.iexec.commons.poco.chain.ChainTaskStatus.UNSET;
import static com.iexec.resultproxy.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProxyServiceTest {
    private static final String RESULT_HASH = "0x97f68778e2fa9d60e58ceb64de2c0e72e309400c3168c69499db2140fad28039";
    private static final String WALLET_ADDRESS = "0x123abc";
    private static final String WORKER_ADDRESS = "0xabc123";
    /**
     * Contains a valid result zip, with the following files:
     * <ul>
     *     <li>{@literal computed.json}, pointing on `/iexec_out/result.txt`
     *     <li>{@literal result.txt}
     * </ul>
     */
    private static final byte[] RESULT_ZIP = Base64.getDecoder().decode("UEsDBBQACAgIADhKL1cAAAAAAAAAAAAAAAANAAAAY29tcHV0ZWQuanNvbqtWSkktSS3KzczLLC7JTNbNLy0pKC3RLUgsyVCyUlDSz0ytSE2OB4rqF6UWl+aU6JVUlCjVAgBQSwcIv08alTcAAAA2AAAAUEsDBBQACAgIADhKL1cAAAAAAAAAAAAAAAAKAAAAcmVzdWx0LnR4dG2NsQrDMAxEd33FdbOhoL1boUM/wnCE4MFg2qEZOvjjK9tKhhIbW9LTnQQC6G8ET7DX/5CQhnlJHol3FQkGAuOAU9ENPaqZE45k6h0NSC/N0Ff231DzgW3qbWxqDqeCZuDYkfpCehIQoeYHEtMOR4NRcHKantF55JlrfV9xr2XNF5HHsi2fvCFoyd+8srw03iDyA1BLBwgqyEkSkwAAAE0BAABQSwECFAAUAAgICAA4Si9Xv08alTcAAAA2AAAADQAAAAAAAAAAAAAAAAAAAAAAY29tcHV0ZWQuanNvblBLAQIUABQACAgIADhKL1cqyEkSkwAAAE0BAAAKAAAAAAAAAAAAAAAAAHIAAAByZXN1bHQudHh0UEsFBgAAAAACAAIAcwAAAD0BAAAAAA==");

    private static final ChainDeal STD_DEAL = ChainDeal.builder()
            .chainDealId(CHAIN_DEAL_ID)
            .tag(BytesUtils.toByte32HexString(0L))
            .build();

    private static final ChainDeal TEE_DEAL = ChainDeal.builder()
            .tag(TeeUtils.TEE_SCONE_ONLY_TAG)
            .requester(WALLET_ADDRESS)
            .build();

    private static final ChainTask CHAIN_TASK = ChainTask.builder()
            .chainTaskId(CHAIN_TASK_ID)
            .dealid(CHAIN_DEAL_ID)
            .status(ChainTaskStatus.REVEALING)
            .build();

    /**
     * {@link ChainContribution} with a hash
     * corresponding to {@link ProxyServiceTest#RESULT_ZIP} hash,
     * including a fixed ChainTask ID: {@literal 0x59d9b6c36d6db89bae058ff55de6e4d6a6f6e0da3f9ea02297fc8d6d5f5cedf1}.
     */
    private static final ChainContribution CHAIN_CONTRIBUTION = ChainContribution.builder()
            .status(REVEALED)
            .resultHash(RESULT_HASH)
            .build();

    private static final ResultModel RESULT_MODEL = ResultModel.builder()
            .chainTaskId(CHAIN_TASK_ID)
            .zip(RESULT_ZIP)
            .build();

    private static final ResultModel RESULT_MODEL_WITH_SIGN = ResultModel.builder()
            .chainTaskId(CHAIN_TASK_ID)
            .enclaveSignature("0x3")
            .zip(RESULT_ZIP)
            .build();

    @TempDir
    File tmpFolder;

    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private IexecHubService iexecHubService;
    @Mock
    private IpfsResultService ipfsResultService;

    @Spy
    @InjectMocks
    private ProxyService proxyService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(false);
    }

    @Test
    void isNotAbleToUploadSinceResultAlreadyExistsWithIpfs() {
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(true);

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verifyNoInteractions(authorizationService, iexecHubService);
    }

    @Test
    void isNotAbleToUploadSinceNoChainTask() {
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.empty());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verify(iexecHubService).getChainTask(CHAIN_TASK_ID);
        verifyNoInteractions(authorizationService);
    }

    @Test
    void isNotAbleToUploadSinceNoChainDeal() {
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(CHAIN_TASK));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.empty());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verify(iexecHubService).getChainTask(CHAIN_TASK_ID);
        verify(iexecHubService).getChainDeal(CHAIN_DEAL_ID);
        verifyNoInteractions(authorizationService);
    }

    // region STD task
    @Test
    void isNotAbleToUploadSinceNoChainContribution() {
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(CHAIN_TASK));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(STD_DEAL));
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.empty());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceCannotWriteZip() {
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(CHAIN_TASK));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(STD_DEAL));
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn("/this/path/does/not/exist");

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceWrongHash() {
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(CHAIN_TASK));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(STD_DEAL));
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        final ResultModel model = ResultModel.builder().chainTaskId(CHAIN_TASK_ID).zip(new byte[0]).build();
        assertThat(proxyService.canUploadResult(model, WALLET_ADDRESS)).isFalse();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceChainStatusIsNotRevealedWithIpfs() {
        ChainContribution chainContribution = ChainContribution.builder().status(CONTRIBUTED).resultHash(RESULT_HASH).build();
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(CHAIN_TASK));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(STD_DEAL));
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(chainContribution));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isAbleToUploadStandardTaskResult() {
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(CHAIN_TASK));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(STD_DEAL));
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isTrue();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
        verifyNoInteractions(authorizationService);
    }
    // endregion

    // region TEE tasks with enclave signature
    @Test
    void isNotAbleToUploadSinceEnclaveSignatureIsNotValid() {
        final ChainTask activeTask = getChainTask(ACTIVE);
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(activeTask));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(TEE_DEAL));
        when(authorizationService.checkEnclaveSignature(RESULT_MODEL_WITH_SIGN, WORKER_ADDRESS)).thenReturn(false);

        assertThat(proxyService.canUploadResult(RESULT_MODEL_WITH_SIGN, WORKER_ADDRESS)).isFalse();

        verify(authorizationService).checkEnclaveSignature(RESULT_MODEL_WITH_SIGN, WORKER_ADDRESS);
    }

    @Test
    void isAbleToUploadTeeTaskResultWithEnclaveSignature() {
        final ChainTask activeTask = getChainTask(ACTIVE);
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(activeTask));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(TEE_DEAL));
        when(authorizationService.checkEnclaveSignature(RESULT_MODEL_WITH_SIGN, WORKER_ADDRESS)).thenReturn(true);

        assertThat(proxyService.canUploadResult(RESULT_MODEL_WITH_SIGN, WORKER_ADDRESS)).isTrue();

        verify(authorizationService).checkEnclaveSignature(RESULT_MODEL_WITH_SIGN, WORKER_ADDRESS);
    }
    // endregion

    // region TEE tasks no enclave signature
    @Test
    void isNotAbleToUploadSinceTaskNotActive() {
        final ChainTask chainTask = getChainTask(UNSET);

        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(TEE_DEAL));

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verifyNoInteractions(authorizationService);
    }

    @Test
    void isAbleToUploadTeeTaskResultWithoutEnclaveSignature() {
        final ChainTask chainTask = getChainTask(ACTIVE);
        when(iexecHubService.getChainTask(CHAIN_TASK_ID)).thenReturn(Optional.of(chainTask));
        when(iexecHubService.getChainDeal(CHAIN_DEAL_ID)).thenReturn(Optional.of(TEE_DEAL));

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isTrue();

        verify(ipfsResultService).doesResultExist(CHAIN_TASK_ID);
        verifyNoInteractions(authorizationService);
    }
    // endregion
}

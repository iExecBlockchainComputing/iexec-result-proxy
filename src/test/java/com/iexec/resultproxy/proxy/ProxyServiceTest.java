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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProxyServiceTest {
    private static final String CHAIN_TASK_ID = "0x59d9b6c36d6db89bae058ff55de6e4d6a6f6e0da3f9ea02297fc8d6d5f5cedf1";
    private static final String RESULT_HASH = "0x865e1ebff87de7928040a42383b46690a12a988b278eb880e0e641f5da3cc9d1";
    private static final String WALLET_ADDRESS = "0x123abc";
    /**
     * Contains a valid result zip, with the following files:
     * <ul>
     *     <li>{@literal computed.json}, pointing on `/iexec_out/result.txt`
     *     <li>{@literal result.txt}
     * </ul>
     */
    private static final byte[] RESULT_ZIP = Base64.getDecoder().decode("UEsDBBQACAgIADhKL1cAAAAAAAAAAAAAAAANAAAAY29tcHV0ZWQuanNvbqtWSkktSS3KzczLLC7JTNbNLy0pKC3RLUgsyVCyUlDSz0ytSE2OB4rqF6UWl+aU6JVUlCjVAgBQSwcIv08alTcAAAA2AAAAUEsDBBQACAgIADhKL1cAAAAAAAAAAAAAAAAKAAAAcmVzdWx0LnR4dG2NsQrDMAxEd33FdbOhoL1boUM/wnCE4MFg2qEZOvjjK9tKhhIbW9LTnQQC6G8ET7DX/5CQhnlJHol3FQkGAuOAU9ENPaqZE45k6h0NSC/N0Ff231DzgW3qbWxqDqeCZuDYkfpCehIQoeYHEtMOR4NRcHKantF55JlrfV9xr2XNF5HHsi2fvCFoyd+8srw03iDyA1BLBwgqyEkSkwAAAE0BAABQSwECFAAUAAgICAA4Si9Xv08alTcAAAA2AAAADQAAAAAAAAAAAAAAAAAAAAAAY29tcHV0ZWQuanNvblBLAQIUABQACAgIADhKL1cqyEkSkwAAAE0BAAAKAAAAAAAAAAAAAAAAAHIAAAByZXN1bHQudHh0UEsFBgAAAAACAAIAcwAAAD0BAAAAAA==");
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

    @TempDir
    File tmpFolder;

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
    }

    @Test
    void isNotAbleToUploadSinceNoChainContribution() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(false);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.empty());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceCannotWriteZip() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(false);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn("/this/path/does/not/exist");

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceWrongHash() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(false);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        final ResultModel model = ResultModel.builder().chainTaskId(CHAIN_TASK_ID).zip(new byte[0]).build();
        assertThat(proxyService.canUploadResult(model, WALLET_ADDRESS)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceResultAlreadyExistsWithIpfs() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(true);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService, never()).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceChainStatusIsNotRevealedWithIpfs() {
        ChainContribution chainContribution = ChainContribution.builder().status(CONTRIBUTED).resultHash(RESULT_HASH).build();
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(true);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(chainContribution));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService, never()).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isAbleToUploadWithIpfs() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(false);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(RESULT_MODEL, WALLET_ADDRESS)).isTrue();

        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
    }
}

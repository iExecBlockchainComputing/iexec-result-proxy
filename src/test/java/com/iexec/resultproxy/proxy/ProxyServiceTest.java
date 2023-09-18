package com.iexec.resultproxy.proxy;

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

import static com.iexec.commons.poco.chain.ChainContributionStatus.REVEALED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProxyServiceTest {
    private static final String CHAIN_TASK_ID = "0x59d9b6c36d6db89bae058ff55de6e4d6a6f6e0da3f9ea02297fc8d6d5f5cedf1";
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
            .resultHash("0x865e1ebff87de7928040a42383b46690a12a988b278eb880e0e641f5da3cc9d1")
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
        when(iexecHubService.isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED)).thenReturn(true);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.empty());

        assertThat(proxyService.canUploadResult(CHAIN_TASK_ID, WALLET_ADDRESS, RESULT_ZIP)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceCantWriteZip() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(false);
        when(iexecHubService.isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED)).thenReturn(true);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn("/this/path/does/not/exist");

        assertThat(proxyService.canUploadResult(CHAIN_TASK_ID, WALLET_ADDRESS, RESULT_ZIP)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceWrongHash() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(false);
        when(iexecHubService.isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED)).thenReturn(true);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(CHAIN_TASK_ID, WALLET_ADDRESS, new byte[] {})).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED);
        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceResultAlreadyExistsWithIpfs() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(true);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(CHAIN_TASK_ID, WALLET_ADDRESS, RESULT_ZIP)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService, never()).isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED);
        verify(iexecHubService, never()).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isNotAbleToUploadSinceChainStatusIsNotRevealedWithIpfs() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(true);
        when(iexecHubService.isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED)).thenReturn(false);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(CHAIN_TASK_ID, WALLET_ADDRESS, RESULT_ZIP)).isFalse();

        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED);
        verify(iexecHubService, never()).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
    }

    @Test
    void isAbleToUploadWithIpfs() {
        when(iexecHubService.isTeeTask(CHAIN_TASK_ID)).thenReturn(false);
        when(ipfsResultService.doesResultExist(CHAIN_TASK_ID)).thenReturn(false);
        when(iexecHubService.isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED)).thenReturn(true);
        when(iexecHubService.getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS)).thenReturn(Optional.of(CHAIN_CONTRIBUTION));
        when(proxyService.getResultFolderPath(CHAIN_TASK_ID)).thenReturn(tmpFolder.getAbsolutePath());

        assertThat(proxyService.canUploadResult(CHAIN_TASK_ID, WALLET_ADDRESS, RESULT_ZIP)).isTrue();

        verify(iexecHubService).getChainContribution(CHAIN_TASK_ID, WALLET_ADDRESS);
        verify(iexecHubService).isTeeTask(CHAIN_TASK_ID);
        verify(proxyService).isResultFound(CHAIN_TASK_ID);
        verify(iexecHubService).isStatusTrueOnChain(CHAIN_TASK_ID, WALLET_ADDRESS, REVEALED);
    }
}

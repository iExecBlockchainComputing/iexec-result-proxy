package com.iexec.resultproxy.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.iexec.resultproxy.chain.IexecHubService;
import com.iexec.resultproxy.ipfs.IpfsResultService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProxyServiceTest {

    @Mock
    private IexecHubService iexecHubService;

    @Mock
    private IpfsResultService ipfsResultService;


    @InjectMocks
    private ProxyService proxyService;

    private String chainTaskId;
    private String walletAddress;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        chainTaskId = "0x1";
        walletAddress = "0x123abc";
    }

    @Test
    public void isNotAbleToUploadSinceResultAlreadyExistsWithIpfs() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(true);
        when(ipfsResultService.doesResultExist(chainTaskId)).thenReturn(true);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress)).isFalse();
    }

    @Test
    public void isNotAbleToUploadSinceChainStatusIsNotRevealedWithIpfs() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(true);
        when(ipfsResultService.doesResultExist(chainTaskId)).thenReturn(true);
        when(iexecHubService.isStatusTrueOnChain(any(), any(), any())).thenReturn(false);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress)).isFalse();
    }

    @Test
    public void isAbleToUploadWithIpfs() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(true);
        when(ipfsResultService.doesResultExist(chainTaskId)).thenReturn(false);
        when(iexecHubService.isStatusTrueOnChain(any(), any(), any())).thenReturn(true);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress)).isTrue();
    }
}

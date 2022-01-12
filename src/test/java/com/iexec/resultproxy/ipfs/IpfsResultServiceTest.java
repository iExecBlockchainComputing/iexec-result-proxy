package com.iexec.resultproxy.ipfs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.iexec.common.utils.BytesUtils;
import com.iexec.resultproxy.chain.IexecHubService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


class IpfsResultServiceTest {

    @Mock
    private IexecHubService iexecHubService;

    @Mock
    private IpfsService ipfsService;

    @InjectMocks
    private IpfsResultService ipfsResultService;

    private String chainTaskId;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        chainTaskId = "0x1";
    }

    /*
    @Test
    void shouldGetIpfsHashFromChainTaskId() {
        String resultLink = "/ipfs/QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA";
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId).equals("QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA")).isTrue();
    }

    @Test
    void shouldNotGetIpfsHashFromChainTaskIdSinceNoTaskResult() {
        String resultLink = "/";
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId)).isEmpty();
    }

    @Test
    void shouldNotGetIpfsHashFromChainTaskIdSinceNotHexaString() {
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn("0xefg");

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId)).isEmpty();
    }

    @Test
    void shouldNotGetIpfsHashFromChainTaskIdSinceNotIpfsLink() {
        String resultLink = "https://customrepo.com/results/abc";
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId)).isEmpty();
    }

    @Test
    void shouldNotGetIpfsHashFromChainTaskIdSinceNotIpfsHash() {
        String resultLink = "/ipfs/ipfs/123";
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId)).isEmpty();
    }

    */

}
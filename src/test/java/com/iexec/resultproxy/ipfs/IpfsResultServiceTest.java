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


public class IpfsResultServiceTest {

    @Mock
    private IexecHubService iexecHubService;

    @Mock
    private IpfsService ipfsService;

    @InjectMocks
    private IpfsResultService ipfsResultService;

    private String chainTaskId;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        chainTaskId = "0x1";
    }

    @Test
    public void shouldGetIpfsHashFromChainTaskId() {
        String resultLink = "/ipfs/QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA";
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId).equals("QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA")).isTrue();
    }

    @Test
    public void shouldNotGetIpfsHashFromChainTaskIdSinceNoTaskResult() {
        String resultLink = "/";
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId)).isEmpty();
    }

    @Test
    public void shouldNotGetIpfsHashFromChainTaskIdSinceNotHexaString() {
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn("0xefg");

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId)).isEmpty();
    }

    @Test
    public void shouldNotGetIpfsHashFromChainTaskIdSinceNotIpfsLink() {
        String resultLink = "https://customrepo.com/results/abc";
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId)).isEmpty();
    }

    @Test
    public void shouldNotGetIpfsHashFromChainTaskIdSinceNotIpfsHash() {
        String resultLink = "/ipfs/ipfs/123";
        when(iexecHubService.getTaskResults(chainTaskId, 0)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getIpfsHashFromChainTaskId(chainTaskId)).isEmpty();
    }

}
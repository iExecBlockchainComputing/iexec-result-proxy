package com.iexec.resultproxy.ipfs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;;


public class IpfsServiceTest {

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldBeIpfsHash() {
        String hash = "QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA";
        assertThat(IpfsService.isIpfsHash(hash)).isTrue();
    }

    @Test
    public void shouldBeIpfsHashSinceWrongLength() {
        String hash = "QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbU";
        assertThat(IpfsService.isIpfsHash(hash)).isFalse();
    }

    @Test
    public void shouldBeIpfsHashSinceNotIpfsHash() {
        String hash = "abcd";
        assertThat(IpfsService.isIpfsHash(hash)).isFalse();
    }

    @Test
    public void shouldBeIpfsHashSinceNotIpfsEmpty() {
        String hash = "";
        assertThat(IpfsService.isIpfsHash(hash)).isFalse();
    }


}
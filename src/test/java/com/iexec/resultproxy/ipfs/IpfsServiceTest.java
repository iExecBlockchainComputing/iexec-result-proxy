package com.iexec.resultproxy.ipfs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;


class IpfsServiceTest {

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldBeIpfsHash() {
        String hash = "QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA";
        assertThat(IpfsService.isIpfsHash(hash)).isTrue();
    }

    @Test
    void shouldBeIpfsHashSinceWrongLength() {
        String hash = "QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbU";
        assertThat(IpfsService.isIpfsHash(hash)).isFalse();
    }

    @Test
    void shouldBeIpfsHashSinceNotIpfsHash() {
        String hash = "abcd";
        assertThat(IpfsService.isIpfsHash(hash)).isFalse();
    }

    @Test
    void shouldBeIpfsHashSinceNotIpfsEmpty() {
        String hash = "";
        assertThat(IpfsService.isIpfsHash(hash)).isFalse();
    }


}
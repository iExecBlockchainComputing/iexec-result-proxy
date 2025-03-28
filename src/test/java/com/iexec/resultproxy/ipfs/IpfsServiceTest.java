/*
 * Copyright 2020-2025 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy.ipfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IpfsServiceTest {

    @Mock
    private IpfsConfig ipfsConfig;

    static Stream<Arguments> testHashData() {
        return Stream.of(
                Arguments.of("QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA", true),
                Arguments.of("QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbU", false),
                Arguments.of("abcd", false),
                Arguments.of("", false)
        );
    }

    @ParameterizedTest
    @MethodSource("testHashData")
    void shouldBeIpfsHash(final String hash, final boolean expected) {
        assertThat(IpfsService.isIpfsHash(hash)).isEqualTo(expected);
    }

    static Stream<Arguments> testURLData() {
        return Stream.of(
                Arguments.of("http://127.0.0.1:5001", "/ip4/127.0.0.1/tcp/5001"),
                Arguments.of("http://localhost:5001", "/ip4/.+/tcp/5001"),
                Arguments.of("http://127.0.0.1", "/ip4/127.0.0.1/tcp/80"),
                Arguments.of("http://localhost", "/ip4/.+/tcp/80")
        );
    }

    @ParameterizedTest
    @MethodSource("testURLData")
    void shouldConstructMultiAddress(final String url, final String expectedMultiAddress) throws Exception {
        when(ipfsConfig.getUrl()).thenReturn(url);
        final IpfsService ipfsService = new IpfsService(ipfsConfig);
        final String multiAddress = getMultiAddress(ipfsService);
        assertThat(multiAddress).matches(expectedMultiAddress);
    }

    @Test
    void shouldThrowExceptionForInvalidURL() {
        when(ipfsConfig.getUrl()).thenReturn("invalid:url:format");
        final Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new IpfsService(ipfsConfig));
        assertThat(exception.getMessage()).contains("Invalid IPFS URL");
        assertThat(exception.getCause()).isInstanceOf(MalformedURLException.class);
    }

    @Test
    void shouldThrowExceptionForInvalidHostname() {
        when(ipfsConfig.getUrl()).thenReturn("http://nonexistent-host.example:5001");
        final Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new IpfsService(ipfsConfig));
        assertThat(exception.getMessage()).contains("Invalid IPFS URL");
        assertThat(exception.getCause()).isInstanceOf(UnknownHostException.class);
    }

    @Test
    void shouldThrowExceptionWhenUrlIsInvalid() {
        when(ipfsConfig.getUrl()).thenReturn("invalid:url:format");
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IpfsService(ipfsConfig)
        );
        assertThat(exception.getMessage()).contains("Invalid IPFS URL");
        assertThat(exception.getCause()).isInstanceOf(MalformedURLException.class);
    }

    // Helper method to access private field for testing
    private String getMultiAddress(IpfsService service) throws Exception {
        final Field field = IpfsService.class.getDeclaredField("multiAddress");
        field.setAccessible(true);
        return (String) field.get(service);
    }

}

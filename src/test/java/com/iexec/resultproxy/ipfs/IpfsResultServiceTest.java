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

package com.iexec.resultproxy.ipfs;

import com.iexec.commons.poco.utils.BytesUtils;
import com.iexec.resultproxy.ipfs.task.IpfsNameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class IpfsResultServiceTest {

    @Mock
    private IpfsNameService ipfsNameService;

    @Mock
    private IpfsService ipfsService;

    @Spy
    @InjectMocks
    private IpfsResultService ipfsResultService;

    private String chainTaskId;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        chainTaskId = "0x1";
    }

    // region addResult
    @Test
    void shouldNotAddResult() {
        when(ipfsNameService.getIpfsHashForTask(anyString())).thenReturn("QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA");
        assertThat(ipfsResultService.addResult(chainTaskId, new byte[0])).isEmpty();
    }

    @Test
    void shouldAddResult() {
        when(ipfsNameService.getIpfsHashForTask(anyString())).thenReturn("");
        when(ipfsService.add(any(), any())).thenReturn("QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA");
        assertThat(ipfsResultService.addResult(chainTaskId, new byte[0]))
                .isEqualTo("/ipfs/QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA");
    }
    // endregion

    // region doesResultExist
    @Test
    void shouldResultExist() {
        doReturn(Optional.of(new byte[0])).when(ipfsResultService).getResult(chainTaskId);
        assertThat(ipfsResultService.doesResultExist(chainTaskId)).isTrue();
    }

    @Test
    void shouldResultNotExist() {
        doReturn(Optional.empty()).when(ipfsResultService).getResult(chainTaskId);
        assertThat(ipfsResultService.doesResultExist(chainTaskId)).isFalse();
    }
    // endregion

    // region getResult
    @Test
    void shouldGetResult() {
        String resultLink = "/ipfs/QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA";
        when(ipfsNameService.getIpfsHashForTask(chainTaskId)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));
        assertThat(ipfsResultService.getResult(chainTaskId)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource(value = "provideIpfsHash")
    void shouldNotGetResultWhenBadIpfsHash(String ipfsHash) {
        when(ipfsNameService.getIpfsHashForTask(chainTaskId)).thenReturn(ipfsHash);
        assertThat(ipfsResultService.getResult(chainTaskId)).isEmpty();
    }

    static Stream<Arguments> provideIpfsHash() {
        return Stream.of(
                Arguments.of("0xefg"),
                Arguments.of(BytesUtils.bytesToString("/".getBytes())),
                Arguments.of(BytesUtils.bytesToString("/ipfs/ipfs/123".getBytes())),
                Arguments.of(BytesUtils.bytesToString("https://customrepo.com/results/abc".getBytes()))
        );
    }
    // endregion

}

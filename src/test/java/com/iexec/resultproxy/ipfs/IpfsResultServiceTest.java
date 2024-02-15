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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class IpfsResultServiceTest {

    @Mock
    private IpfsNameService ipfsNameService;

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

    @Test
    void shouldGetIpfsHashFromChainTaskId() {
        String resultLink = "/ipfs/QmfZ88JXmx2FJsAxT4ZsJBVhBUXdPoRbDZhbkSS1WsMbUA";
        when(ipfsNameService.getIpfsHashForTask(chainTaskId)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getResult(chainTaskId)).isNotNull();
    }

    @Test
    void shouldNotGetIpfsHashFromChainTaskIdSinceNoTaskResult() {
        String resultLink = "/";
        when(ipfsNameService.getIpfsHashForTask(chainTaskId)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getResult(chainTaskId)).isEmpty();
    }

    @Test
    void shouldNotGetIpfsHashFromChainTaskIdSinceNotHexaString() {
        when(ipfsNameService.getIpfsHashForTask(chainTaskId)).thenReturn("0xefg");

        assertThat(ipfsResultService.getResult(chainTaskId)).isEmpty();
    }

    @Test
    void shouldNotGetIpfsHashFromChainTaskIdSinceNotIpfsLink() {
        String resultLink = "https://customrepo.com/results/abc";
        when(ipfsNameService.getIpfsHashForTask(chainTaskId)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getResult(chainTaskId)).isEmpty();
    }

    @Test
    void shouldNotGetIpfsHashFromChainTaskIdSinceNotIpfsHash() {
        String resultLink = "/ipfs/ipfs/123";
        when(ipfsNameService.getIpfsHashForTask(chainTaskId)).thenReturn(BytesUtils.bytesToString(resultLink.getBytes()));

        assertThat(ipfsResultService.getResult(chainTaskId)).isEmpty();
    }

}

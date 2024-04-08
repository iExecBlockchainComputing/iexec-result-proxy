/*
 * Copyright 2024-2024 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy;

import com.iexec.commons.poco.chain.ChainDeal;
import com.iexec.commons.poco.chain.ChainTask;
import com.iexec.commons.poco.chain.ChainTaskStatus;
import com.iexec.commons.poco.tee.TeeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    public static final String CHAIN_DEAL_ID = "0x8012c1515a23e0d7ef07b7de780343df2fd18034ca55eea7202cab814603d288";
    public static final String CHAIN_TASK_ID = "0x877210dbec7b8461e396751e311b574d6b6909e3618dd0622f7182eaffdc6901";

    public static final String POOL_ADDRESS = "0xc911f9345717ba7c8ec862ce002af3e058df84e4";
    public static final String POOL_PRIVATE = "0xe2a973b083fae8043543f15313955aecee9de809a318656c1cfb22d3a6d52de1";
    public static final String POOL_WRONG_SIGNATURE = "0xf869daaca2407b7eabd27c3c4c5a3f3565172ca7211ac1d8bfacea2beb511a4029446a07cccc0884"
            + "c2193b269dfb341461db8c680a8898bb53862d6e48340c2e1b";

    public static ChainDeal getChainDeal() {
        return ChainDeal.builder()
                .poolOwner(POOL_ADDRESS)
                .tag(TeeUtils.TEE_SCONE_ONLY_TAG)
                .build();
    }

    public static ChainTask getChainTask(ChainTaskStatus status) {
        return ChainTask.builder()
                .dealid(CHAIN_DEAL_ID)
                .finalDeadline(Instant.now().plus(5L, ChronoUnit.SECONDS).toEpochMilli())
                .status(status)
                .build();
    }
}

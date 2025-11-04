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
import com.iexec.commons.poco.order.OrderTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    public static final String CHAIN_DEAL_ID = "0x8012c1515a23e0d7ef07b7de780343df2fd18034ca55eea7202cab814603d288";
    public static final String CHAIN_TASK_ID = "0x877210dbec7b8461e396751e311b574d6b6909e3618dd0622f7182eaffdc6901";

    public static final String POOL_ADDRESS = "0xc911f9345717ba7c8ec862ce002af3e058df84e4";
    public static final String POOL_PRIVATE = "0xe2a973b083fae8043543f15313955aecee9de809a318656c1cfb22d3a6d52de1";
    public static final String POOL_WRONG_SIGNATURE = "0xf869daaca2407b7eabd27c3c4c5a3f3565172ca7211ac1d8bfacea2beb511a4029446a07cccc0884"
            + "c2193b269dfb341461db8c680a8898bb53862d6e48340c2e1b";

    public static final String RESULT_DIGEST = "0x3210";
    public static final String RESULT_HASH = "0x97f68778e2fa9d60e58ceb64de2c0e72e309400c3168c69499db2140fad28039";
    public static final String WALLET_ADDRESS = "0x123abc";
    public static final String WORKER_ADDRESS = "0xabc123";

    public static Optional<ChainDeal> getChainDeal(final OrderTag tag) {
        return Optional.of(ChainDeal.builder()
                .poolOwner(POOL_ADDRESS)
                .tag(tag.getValue())
                .requester(WALLET_ADDRESS)
                .build());
    }

    public static Optional<ChainTask> getChainTask(final ChainTaskStatus status) {
        return Optional.of(ChainTask.builder()
                .dealid(CHAIN_DEAL_ID)
                .finalDeadline(Instant.now().plus(5L, ChronoUnit.SECONDS).toEpochMilli())
                .status(status)
                .build());
    }
}

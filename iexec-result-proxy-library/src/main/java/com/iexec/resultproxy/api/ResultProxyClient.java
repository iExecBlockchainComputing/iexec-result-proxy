/*
 * Copyright 2022-2024 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy.api;

import com.iexec.common.result.ResultModel;
import com.iexec.commons.poco.eip712.entity.EIP712Challenge;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * Interface allowing to instantiate a Feign client targeting Result Proxy REST endpoints.
 * <p>
 * To create the client, see the related builder.
 *
 * @see ResultProxyClientBuilder
 */
public interface ResultProxyClient {

    @Deprecated(forRemoval = true)
    @RequestLine("GET /results/challenge?chainId={chainId}")
    EIP712Challenge getChallenge(@Param("chainId") int chainId);

    @Deprecated(forRemoval = true)
    @RequestLine("POST /results/login?chainId={chainId}")
    String login(@Param("chainId") int chainId, String token);

    @RequestLine("POST /v1/results")
    @Headers("Authorization: {authorization}")
    String addResult(
            @Param("authorization") String authorization,
            ResultModel model
    );

    @RequestLine("HEAD /v1/results/{chainTaskId}")
    @Headers("Authorization: {authorization}")
    String isResultUploaded(
            @Param("authorization") String authorization,
            @Param("chainTaskId") String chainTaskId
    );

    @RequestLine("GET /v1/results/{chainTaskId}/ipfshash")
    String getIpfsHashForTask(@Param("chainTaskId") String chainTaskId);

}

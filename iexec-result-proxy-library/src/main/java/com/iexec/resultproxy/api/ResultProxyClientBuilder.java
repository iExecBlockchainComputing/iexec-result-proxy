/*
 * Copyright 2022 IEXEC BLOCKCHAIN TECH
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

import com.iexec.common.utils.FeignBuilder;
import feign.Logger;

/**
 * Creates Feign client instances to query REST endpoints described in {@link ResultProxyClient}.
 * @see FeignBuilder
 */
public class ResultProxyClientBuilder {

    private ResultProxyClientBuilder() {}

    public static ResultProxyClient getInstance(Logger.Level logLevel, String url) {
        return FeignBuilder.createBuilder(logLevel)
                .target(ResultProxyClient.class, url);
    }

}

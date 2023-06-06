/*
 * Copyright 2020-2023 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy.chain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "chain")
//TODO: validate configuration property names and use the same set of names everywhere (blockchain-adapter-api, sms)
public class ChainConfig {
    private final int id;
    private final boolean sidechain;
    private final String privateAddress;
    private final Duration blockTime;
    private final String hubAddress;
    private final float gasPriceMultiplier;
    private final long gasPriceCap;
}

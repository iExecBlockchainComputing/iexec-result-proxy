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

package com.iexec.resultproxy.chain;

import com.iexec.commons.poco.chain.validation.ValidNonZeroEthereumAddress;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Value;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Value
@ConfigurationProperties(prefix = "chain")
//TODO: validate configuration property names and use the same set of names everywhere (blockchain-adapter-api, sms)
public class ChainConfig {
    @Positive
    @NotNull
    int id;

    boolean sidechain;

    @URL
    @NotEmpty
    String privateAddress;

    @ValidNonZeroEthereumAddress
    String hubAddress;

    @Positive
    @NotNull
    Duration blockTime;

    @Positive
    float gasPriceMultiplier;

    @PositiveOrZero
    long gasPriceCap;
}

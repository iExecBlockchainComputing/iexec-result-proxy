/*
 * Copyright 2025 IEXEC BLOCKCHAIN TECH
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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChainConfigTest {
    private static final String IEXEC_NODE_ADDRESS = "https://bellecour.iex.ec";
    private static final String IEXEC_HUB_ADDRESS = "0x1a69b2eb604db8eba185df03ea4f5288dcbbd248";

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (final ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void idMustBePositive() {
        final ChainConfig config = new ChainConfig(
                0,  // invalid id
                false,
                IEXEC_NODE_ADDRESS,
                IEXEC_HUB_ADDRESS,
                Duration.ofMillis(100),
                1.5f,
                100L
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must be greater than 0");
    }

    @Test
    void nodeAddressMustBeValidURL() {
        final ChainConfig config = new ChainConfig(
                1,
                false,
                "invalid-url",  // invalid URL
                IEXEC_HUB_ADDRESS,
                Duration.ofMillis(100),
                1.5f,
                100L
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must be a valid URL");
    }

    @Test
    void nodeAddressMustNotBeEmpty() {
        final ChainConfig config = new ChainConfig(
                1,
                false,
                "",  // empty nodeAddress
                IEXEC_HUB_ADDRESS,
                Duration.ofMillis(100),
                1.5f,
                100L
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must not be empty");
    }

    @Test
    void hubAddressMustBeValidEthereumAddress() {
        final ChainConfig config = new ChainConfig(
                1,
                false,
                IEXEC_NODE_ADDRESS,
                "0x0",  // invalid address
                Duration.ofMillis(100),
                1.5f,
                100L
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Invalid non-zero Ethereum address");
    }

    @Test
    void blockTimeMustBeAtLeast100ms() {
        final ChainConfig config = new ChainConfig(
                1,
                false,
                IEXEC_NODE_ADDRESS,
                IEXEC_HUB_ADDRESS,
                Duration.ofMillis(99),  // <100ms
                1.5f,
                100L
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must be longer than or equal to 100 millis");
    }

    @Test
    void blockTimeMustBeAtMost20Seconds() {
        final ChainConfig config = new ChainConfig(
                1,
                false,
                IEXEC_NODE_ADDRESS,
                IEXEC_HUB_ADDRESS,
                Duration.ofSeconds(21),  // >20s
                1.5f,
                100L
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must be shorter than or equal to 20 seconds");
    }

    @Test
    void blockTimeMustNotBeNull() {
        final ChainConfig config = new ChainConfig(
                1,
                false,
                IEXEC_NODE_ADDRESS,
                IEXEC_HUB_ADDRESS,
                null,  // null blockTime
                1.5f,
                100L
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must not be null");
    }

    @Test
    void gasPriceMultiplierMustBePositive() {
        final ChainConfig config = new ChainConfig(
                1,
                false,
                IEXEC_NODE_ADDRESS,
                IEXEC_HUB_ADDRESS,
                Duration.ofMillis(100),
                0.0f,  // invalid multiplier
                100L
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must be greater than 0");
    }

    @Test
    void gasPriceCapMustBePositiveOrZero() {
        final ChainConfig config = new ChainConfig(
                1,
                false,
                IEXEC_NODE_ADDRESS,
                IEXEC_HUB_ADDRESS,
                Duration.ofMillis(100),
                1.5f,
                -1L  // invalid cap
        );
        final Set<ConstraintViolation<ChainConfig>> violations = validator.validate(config);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must be greater than or equal to 0");
    }
}

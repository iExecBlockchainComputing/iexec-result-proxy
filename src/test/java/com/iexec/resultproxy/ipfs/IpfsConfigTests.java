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

package com.iexec.resultproxy.ipfs;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IpfsConfigTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:5001",
            "http://127.0.0.1:5001",
            "https://ipfs.example.com",
            "http://localhost",
            "http://127.0.0.1"
    })
    void validUrlVariationsShouldPassValidation(String url) {
        IpfsConfig config = new IpfsConfig(url);
        Set<ConstraintViolation<IpfsConfig>> violations = validator.validate(config);
        assertThat(violations).isEmpty();
    }

    @Test
    void emptyShouldFailValidation() {
        IpfsConfig config = new IpfsConfig("");
        Set<ConstraintViolation<IpfsConfig>> violations = validator.validate(config);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("IPFS URL must not be empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-url",
            "://no-protocol.com",
    })
    void invalidUrlShouldFailValidation(String url) {
        IpfsConfig config = new IpfsConfig(url);
        Set<ConstraintViolation<IpfsConfig>> violations = validator.validate(config);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("IPFS URL must be a valid URL");
    }
}

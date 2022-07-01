package com.iexec.resultproxy.challenge;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;


class EIP712ChallengeServiceTest {

    @InjectMocks
    private EIP712ChallengeService eip712ChallengeService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }
}
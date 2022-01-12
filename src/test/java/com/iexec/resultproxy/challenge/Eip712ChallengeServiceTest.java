package com.iexec.resultproxy.challenge;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;


class Eip712ChallengeServiceTest {

    @InjectMocks
    private Eip712ChallengeService eip712ChallengeService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }
}
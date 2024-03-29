package com.iexec.resultproxy.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.iexec.common.security.SignedChallenge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuthorizationServiceTest {

    @Mock
    private EIP712ChallengeService eip712ChallengeService;

    @InjectMocks
    private ChallengeService challengeService;

    private String challenge;
    private String challengeSignature;
    private String address;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        challenge = "0xb7a099c5998bb07a9e30ad6faaa79ddfc70c3475134957de7343ddb13f4c382a";
        challengeSignature = "0x1b0b90d9f17a30d42492c8a2f98a24374600729a98d4e0b663a44ed48b589cab0e445eec300245e590150c7d88340d902c27e0d8673f3257cb8393f647d6c75c1b";
        address = "0xabcd1339Ec7e762e639f4887E2bFe5EE8023E23E";
    }

    @Test
    void isNotAuthorizedToGetResultSinceNullAuthorization() {
        assertThat(challengeService.isSignedChallengeValid(null)).isFalse();
    }

    @Test
    void isNotAuthorizedToGetResultSinceNoChallengeInMap() {
        when(eip712ChallengeService.containsEip712ChallengeString(challenge)).thenReturn(false);
        SignedChallenge authorization = SignedChallenge.builder()
                .challengeHash(challenge)
                .challengeSignature(challengeSignature)
                .walletAddress("0xa")
                .build();
        assertThat(challengeService.isSignedChallengeValid(authorization)).isFalse();
    }

    @Test
    void isNotAuthorizedToGetResultSinceChallengeSignatureIsWrong() {
        when(eip712ChallengeService.containsEip712ChallengeString(challenge)).thenReturn(true);
        SignedChallenge authorization = SignedChallenge.builder()
                .challengeHash(challenge)
                .challengeSignature("0x1b0b90d9f17a30d42492c8a2f98a24374600729a98d4e0b663a44ed48b589cab0e445eec300245e590150c7d88340d902c27e0d8673f3257cb8393f647d6c7dead")
                .walletAddress(address)
                .build();
        assertThat(challengeService.isSignedChallengeValid(authorization)).isFalse();
    }

    @Test
    void isNotAuthorizedToGetResultSinceChallengeSignatureIsBadFormat() {
        when(eip712ChallengeService.containsEip712ChallengeString(challenge)).thenReturn(true);
        SignedChallenge authorization = SignedChallenge.builder()
                .challengeHash(challenge)
                .challengeSignature("0xbad")
                .walletAddress(address)
                .build();
        assertThat(challengeService.isSignedChallengeValid(authorization)).isFalse();
    }

    @Test
    void isNotAuthorizedToGetResultSinceChallengeSignatureIsBadFormat2() {
        when(eip712ChallengeService.containsEip712ChallengeString(challenge)).thenReturn(true);
        SignedChallenge authorization = SignedChallenge.builder()
                .challengeHash(challenge)
                .challengeSignature("1b0b90d9f17a30d42492c8a2f98a24374600729a98d4e0b663a44ed48b589cab0e445eec300245e590150c7d88340d902c27e0d8673f3257cb8393f647d6c7FAKE")
                .walletAddress("0xabcd1339Ec7e762e639f4887E2bFe5EE8023E23E")
                .build();
        assertThat(challengeService.isSignedChallengeValid(authorization)).isFalse();
    }

    @Test
    void shouldNotGetAuthorizationFromTokenSinceNullToken() {
        assertThat(challengeService.tokenToSignedChallengeObject(null)).isNull();
    }

    @Test
    void shouldNotGetAuthorizationFromTokenSinceTokenNotValid() {
        String token = "bad_token";

        assertThat(challengeService.tokenToSignedChallengeObject(token)).isNull();
    }

    @Test
    void shouldGetAuthorizationFromToken() {
        String token = "not_bad_token";

        assertThat(challengeService.tokenToSignedChallengeObject(token)).isNotNull();
    }
}
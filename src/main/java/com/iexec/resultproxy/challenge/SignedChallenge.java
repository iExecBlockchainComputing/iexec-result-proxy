package com.iexec.resultproxy.challenge;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;


@Data
@Builder
@Getter
public class SignedChallenge {

    private String challenge;
    private String challengeSignature;
    private String walletAddress;
}



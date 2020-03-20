package com.iexec.resultproxy.auth;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;


@Data
@Builder
@Getter
public class Authorization {

    private String challenge;
    private String challengeSignature;
    private String walletAddress;
}



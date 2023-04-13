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

package com.iexec.resultproxy.challenge;

import com.iexec.common.chain.eip712.entity.EIP712Challenge;
import com.iexec.common.security.SignedChallenge;
import com.iexec.commons.poco.utils.BytesUtils;
import com.iexec.commons.poco.utils.SignatureUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;

@Slf4j
@Service
public class ChallengeService {

    private final EIP712ChallengeService eip712ChallengeService;

    ChallengeService(EIP712ChallengeService challengeService) {
        this.eip712ChallengeService = challengeService;
    }

    public EIP712Challenge createChallenge(Integer chainId) {
        return eip712ChallengeService.generateEip712Challenge(chainId);
    }

    public void invalidateChallenge(String eip712ChallengeString) {
        eip712ChallengeService.invalidateEip712ChallengeString(eip712ChallengeString);
    }

    public SignedChallenge tokenToSignedChallengeObject(String token) {
        if ((token == null) || (token.split("_").length != 3)) {
            return null;
        }

        String[] parts = token.split("_");
        return SignedChallenge.builder()
                .challengeHash(parts[0])
                .challengeSignature(parts[1])
                .walletAddress(parts[2])
                .build();
    }

    public boolean isSignedChallengeValid(SignedChallenge signedChallenge) {
        if (signedChallenge == null) {
            log.error("Signed challenge should not be null [SignedChallenge:null]");
            return false;
        }

        String eip712ChallengeString = signedChallenge.getChallengeHash();
        String challengeSignature = signedChallenge.getChallengeSignature();
        String walletAddress = signedChallenge.getWalletAddress();

        challengeSignature = Numeric.cleanHexPrefix(challengeSignature);

        if (challengeSignature.length() < 130) {
            log.error("Eip712ChallengeString has a bad signature format [requester:{}]", walletAddress);
            return false;
        }
        String r = challengeSignature.substring(0, 64);
        String s = challengeSignature.substring(64, 128);
        String v = challengeSignature.substring(128, 130);

        //ONE: check if eip712Challenge is in eip712Challenge map
        if (!eip712ChallengeService.containsEip712ChallengeString(eip712ChallengeString)) {
            log.error("Eip712ChallengeString provided doesn't match any challenge [requester:{}]", walletAddress);
            return false;
        }

        //TWO: check if ecrecover on eip712Challenge & signature match address
        if (!SignatureUtils.doesSignatureMatchesAddress(BytesUtils.stringToBytes(r), BytesUtils.stringToBytes(s),
                eip712ChallengeString, StringUtils.lowerCase(walletAddress))) {
            log.error("Signature provided doesn't match walletAddress [requester:{}, sign.r:{}, sign.s:{}, eip712ChallengeString:{}]",
                    walletAddress, r, s, eip712ChallengeString);
            return false;
        }

        return true;
    }

}

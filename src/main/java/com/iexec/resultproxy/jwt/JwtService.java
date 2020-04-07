package com.iexec.resultproxy.jwt;

import java.util.Date;
import java.util.Optional;

import com.iexec.resultproxy.auth.SignedChallenge;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

@Service
public class JwtService {

    @Autowired
    JwtRepository jwtRepository;

    public String getOrCreateJwt(SignedChallenge auth) {
        String jwtString;
        Optional<Jwt> oExistingJwt = findByWalletAddress(auth.getWalletAddress());

        if (oExistingJwt.isPresent()) {
            jwtString = oExistingJwt.get().getJwtString(); // TODO generate new token
        } else {
            jwtString = Jwts.builder().setAudience(auth.getWalletAddress()).setIssuedAt(new Date())
                    .setSubject(RandomStringUtils.randomAlphanumeric(64)).compact();
            save(new Jwt(auth.getWalletAddress(), jwtString));
        }

        return jwtString;
    }

    public boolean isValidJwt(String jwtString) {
        String claimedWalletAddress = getWalletAddressFromJwtString(jwtString);

        Optional<Jwt> oExistingJwt = findByWalletAddress(claimedWalletAddress);

        if (!oExistingJwt.isPresent()) {
            return false;
        }

        if (jwtString.equals(oExistingJwt.get().getJwtString())) {
            return true;
        }

        return false;
    }

    public String getWalletAddressFromJwtString(String jwtString) {
        return Jwts.parser().parseClaimsJwt(jwtString).getBody().getAudience();
    }

    public Optional<Jwt> findByWalletAddress(String walletAddress) {
        return jwtRepository.findByWalletAddress(walletAddress);
    }

    public void save(Jwt jwt) {
        jwtRepository.save(jwt);
    }
}
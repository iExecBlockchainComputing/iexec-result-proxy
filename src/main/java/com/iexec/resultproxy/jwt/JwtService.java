package com.iexec.resultproxy.jwt;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.iexec.resultproxy.challenge.SignedChallenge;

import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

@Service
public class JwtService {
    private final ConcurrentMap<String, Object> locks = ExpiringMap.builder()
            .expiration(1, TimeUnit.HOURS)
            .build();

    private JwtRepository jwtRepository;

    public JwtService(JwtRepository jwtRepository) {
        this.jwtRepository = jwtRepository;        
    }

    public String getOrCreateJwt(SignedChallenge signedChallenge) {
        String jwtString;

        // Synchronizing the following is mandatory:
        // we need to ensure there won't be 2 different JWT issued for the same wallet.
        // This is ensured by making synchronous the get-and-save operation.
        synchronized (locks.computeIfAbsent(signedChallenge.getWalletAddress(), key -> new Object())) {
            Optional<Jwt> oExistingJwt = findByWalletAddress(signedChallenge.getWalletAddress());

            if (oExistingJwt.isPresent()) {
                jwtString = oExistingJwt.get().getJwtString(); // TODO generate new token
            } else {
                jwtString = Jwts.builder().setAudience(signedChallenge.getWalletAddress()).setIssuedAt(new Date())
                        .setSubject(RandomStringUtils.randomAlphanumeric(64)).compact();
                save(new Jwt(signedChallenge.getWalletAddress(), jwtString));
            }
        }
        return jwtString;
    }

    public boolean isValidJwt(String jwtString) {
        String claimedWalletAddress = getWalletAddressFromJwtString(jwtString);

        Optional<Jwt> oExistingJwt = findByWalletAddress(claimedWalletAddress);

        if (!oExistingJwt.isPresent()) {
            return false;
        }

        return jwtString.equals(oExistingJwt.get().getJwtString());
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
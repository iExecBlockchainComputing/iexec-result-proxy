package com.iexec.resultproxy.jwt;

import com.iexec.common.utils.ContextualLockRunner;
import com.iexec.resultproxy.challenge.SignedChallenge;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private final ContextualLockRunner<String> contextualLockRunner = new ContextualLockRunner<>();

    private final JwtRepository jwtRepository;

    public JwtService(JwtRepository jwtRepository) {
        this.jwtRepository = jwtRepository;
    }

    public String getOrCreateJwt(SignedChallenge signedChallenge) {
        final String walletAddress = signedChallenge.getWalletAddress();
        // Synchronizing the following is mandatory:
        // we need to ensure there won't be 2 different JWT issued for the same wallet.
        // This is ensured by making synchronous the get-and-save operation.
        return contextualLockRunner.applyWithLock(walletAddress, this::getOrCreateJwt);
    }

    private String getOrCreateJwt(String walletAddress) {
        String jwtString;
        Optional<Jwt> oExistingJwt = findByWalletAddress(walletAddress);

        if (oExistingJwt.isPresent()) {
            jwtString = oExistingJwt.get().getJwtString(); // TODO generate new token
        } else {
            jwtString = Jwts.builder().setAudience(walletAddress).setIssuedAt(new Date())
                    .setSubject(RandomStringUtils.randomAlphanumeric(64)).compact();
            save(new Jwt(walletAddress, jwtString));
        }
        return jwtString;
    }

    public boolean isValidJwt(String jwtString) {
        String claimedWalletAddress = getWalletAddressFromJwtString(jwtString);

        Optional<Jwt> oExistingJwt = findByWalletAddress(claimedWalletAddress);

        return oExistingJwt.isPresent() && jwtString.equals(oExistingJwt.get().getJwtString());
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
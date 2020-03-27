package com.iexec.resultproxy.jwt;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class JwtService {

    @Autowired JwtRepository jwtRepository;

    public Optional<Jwt> findByWalletAddress(String walletAddress) {
        return jwtRepository.findByWalletAddress(walletAddress);
    }

	public void save(Jwt jwt) {
        jwtRepository.save(jwt);
	}
}
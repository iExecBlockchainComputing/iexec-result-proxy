package com.iexec.resultproxy.jwt;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;


public class JwtService {

    @Autowired JwtRepository jwtRepository;

    public Optional<Jwt> findByWalletAddress(String walletAddress) {
        return jwtRepository.findByWalletAddress(walletAddress);
    }

	public void save(Jwt jwt) {
        jwtRepository.save(jwt);
	}
}
package com.iexec.resultproxy.jwt;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface JwtRepository extends MongoRepository<Jwt, String> {

    Optional<Jwt> findByWalletAddress(String walletAddress);
}

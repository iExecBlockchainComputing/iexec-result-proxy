package com.iexec.resultproxy.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Configuration {

    @Value("${resultRepository.protocol}")
    private String protocol;

    @Value("${resultRepository.host}")
    private String host;

    @Value("${resultRepository.port}")
    private String port;

    public String getResultRepositoryURL() {
        return protocol + "://" + host + ":" + port;
    }
}

package com.iexec.resultproxy.ipfs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "ipfs")
public class IpfsConfig {
    private final String host;
    private final String port;
}

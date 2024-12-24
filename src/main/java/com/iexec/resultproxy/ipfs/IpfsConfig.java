package com.iexec.resultproxy.ipfs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ipfs")
public class IpfsConfig {
    private final String host;
    private final String port;
}

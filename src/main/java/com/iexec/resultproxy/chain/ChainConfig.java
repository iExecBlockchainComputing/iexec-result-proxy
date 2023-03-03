package com.iexec.resultproxy.chain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "chain")
//TODO: validate configuration property names and use the same set of names everywhere (blockchain-adapter-api, sms)
public class ChainConfig {
    private final int id;
    private final boolean sidechain;
    private final String privateAddress;
    private final String hubAddress;
    private final float gasPriceMultiplier;
    private final long gasPriceCap;
}

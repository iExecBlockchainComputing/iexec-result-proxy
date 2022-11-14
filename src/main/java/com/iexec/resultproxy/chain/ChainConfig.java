package com.iexec.resultproxy.chain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "chain")
public class ChainConfig {
    private final int id;
    private final boolean sidechain;
    private final String privateAddress;
    private final String publicAddress;
    private final String hubAddress;
    private final long startBlockNumber;
    private final float gasPriceMultiplier;
    private final long gasPriceCap;
}

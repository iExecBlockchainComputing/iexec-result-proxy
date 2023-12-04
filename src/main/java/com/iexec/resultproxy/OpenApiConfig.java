package com.iexec.resultproxy;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String TITLE = "iExec Result Proxy";

    private final BuildProperties buildProperties;

    public OpenApiConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    /*
     * Swagger URI: /swagger-ui/index.html
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(
                new Info()
                        .title(TITLE)
                        .version(buildProperties.getVersion())
        );
    }
}
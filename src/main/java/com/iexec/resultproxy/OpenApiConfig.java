package com.iexec.resultproxy;

import com.iexec.resultproxy.version.VersionService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private final VersionService versionService;

    public OpenApiConfig(VersionService versionService) {
        this.versionService = versionService;
    }

    /*
     * Swagger URI: /swagger-ui/index.html
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(
                new Info()
                        .title("iExec Result Proxy")
                        .version(versionService.getVersion())
        );
    }
}
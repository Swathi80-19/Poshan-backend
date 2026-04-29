package com.poshan.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

@Configuration
public class OpenApiConfig {

    private final String backendBaseUrl;

    public OpenApiConfig(
        @Value("${app.backend-base-url:${BACKEND_BASE_URL:${RENDER_EXTERNAL_URL:}}}") String backendBaseUrl
    ) {
        this.backendBaseUrl = normalizeBaseUrl(backendBaseUrl);
    }

    @Bean
    public OpenAPI poshanOpenApi() {
        final String bearerSchemeName = "bearerAuth";

        OpenAPI openApi = new OpenAPI()
            .info(new Info()
                .title("Poshan Backend API")
                .description("OpenAPI documentation for the Poshan backend services.")
                .version("v1")
                .contact(new Contact().name("Poshan Team")))
            .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
            .components(new Components()
                .addSecuritySchemes(bearerSchemeName, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));

        if (StringUtils.hasText(backendBaseUrl)) {
            openApi.servers(List.of(new Server()
                .url(backendBaseUrl)
                .description("Deployed backend")));
        }

        return openApi;
    }

    private String normalizeBaseUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim().replaceAll("/+$", "");
    }
}

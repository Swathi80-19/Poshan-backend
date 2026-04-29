package com.poshan.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class HomeController {

    private final String applicationName;

    public HomeController(@Value("${spring.application.name:application}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/")
    public Map<String, String> home(HttpServletRequest request) {
        String rawBaseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(request.getContextPath())
            .replaceQuery(null)
            .build()
            .toUriString()
            .replaceAll("/+$", "");
        String baseUrl = normalizeBaseUrl(rawBaseUrl);

        return Map.of(
            "service", applicationName,
            "status", "ok",
            "message", "Backend is running.",
            "swaggerUiUrl", baseUrl + "/swagger-ui.html",
            "openApiUrl", baseUrl + "/v3/api-docs",
            "apiBasePath", "/api"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "service", applicationName,
            "status", "UP"
        );
    }

    private String normalizeBaseUrl(String value) {
        URI uri = URI.create(value);
        int port = uri.getPort();

        if (port == -1 || port == 80 || port == 443) {
            String path = uri.getPath() == null ? "" : uri.getPath().replaceAll("/+$", "");
            return uri.getScheme() + "://" + uri.getHost() + path;
        }

        return value;
    }
}

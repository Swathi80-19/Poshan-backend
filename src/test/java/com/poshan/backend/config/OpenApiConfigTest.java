package com.poshan.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

    @Test
    void addsServerWhenBackendBaseUrlIsConfigured() {
        OpenApiConfig config = new OpenApiConfig("https://poshan-api.onrender.com/");

        var openApi = config.poshanOpenApi();

        assertEquals(1, openApi.getServers().size());
        assertEquals("https://poshan-api.onrender.com", openApi.getServers().getFirst().getUrl());
    }

    @Test
    void skipsServerWhenBackendBaseUrlIsBlank() {
        OpenApiConfig config = new OpenApiConfig("");

        var openApi = config.poshanOpenApi();

        assertTrue(openApi.getServers() == null || openApi.getServers().isEmpty());
    }
}

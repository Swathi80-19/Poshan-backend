package com.poshan.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

class HomeControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new HomeController("poshan-backend")).build();
    }

    @Test
    void homeReturnsJsonWithDocsLinks() throws Exception {
        mockMvc.perform(get("/")
                .with(request -> {
                    request.setSecure(true);
                    request.setScheme("https");
                    return request;
                })
                .header("Host", "poshan-api.onrender.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("poshan-backend"))
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.swaggerUiUrl").value("https://poshan-api.onrender.com/swagger-ui.html"))
            .andExpect(jsonPath("$.openApiUrl").value("https://poshan-api.onrender.com/v3/api-docs"))
            .andExpect(jsonPath("$.apiBasePath").value("/api"));
    }

    @Test
    void healthReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("poshan-backend"))
            .andExpect(jsonPath("$.status").value("UP"));
    }
}

package com.poshan.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.repository.NutritionistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.cors.allowed-origins=http://localhost:5173",
    "app.cors.allowed-origin-patterns=http://localhost:*"
})
class NutritionistControllerVisibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NutritionistRepository nutritionistRepository;

    @BeforeEach
    void setUp() {
        nutritionistRepository.deleteAll();
    }

    @Test
    void publicNutritionistDirectoryIncludesCreatedNutritionists() throws Exception {
        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setName("Asha Menon");
        nutritionist.setUsername("asha_menon");
        nutritionist.setEmail("asha@example.com");
        nutritionist.setSpecialization("Clinical Nutrition");
        nutritionistRepository.save(nutritionist);

        mockMvc.perform(get("/api/nutritionists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Asha Menon"))
            .andExpect(jsonPath("$[0].username").value("asha_menon"))
            .andExpect(jsonPath("$[0].specialization").value("Clinical Nutrition"));
    }

    @Test
    void protectedNutritionistWorkspaceStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/nutritionists/me/dashboard"))
            .andExpect(status().isUnauthorized());
    }
}

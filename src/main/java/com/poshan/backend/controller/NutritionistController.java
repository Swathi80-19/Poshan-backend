package com.poshan.backend.controller;

import com.poshan.backend.dto.DashboardResponse;
import com.poshan.backend.dto.NutritionistPatientResponse;
import com.poshan.backend.dto.NutritionistSummaryResponse;
import com.poshan.backend.security.AuthContext;
import com.poshan.backend.service.NutritionistService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutritionists")
public class NutritionistController {

    private final NutritionistService nutritionistService;
    private final AuthContext authContext;

    public NutritionistController(NutritionistService nutritionistService, AuthContext authContext) {
        this.nutritionistService = nutritionistService;
        this.authContext = authContext;
    }

    @GetMapping
    public List<NutritionistSummaryResponse> getNutritionists() {
        return nutritionistService.getNutritionists();
    }

    @GetMapping("/me/patients")
    public List<NutritionistPatientResponse> getPatients() {
        return nutritionistService.getPatients(authContext.requireNutritionistId());
    }

    @GetMapping("/me/dashboard")
    public DashboardResponse getDashboard() {
        return nutritionistService.getNutritionistDashboard(authContext.requireNutritionistId());
    }
}

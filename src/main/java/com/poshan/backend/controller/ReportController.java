package com.poshan.backend.controller;

import com.poshan.backend.dto.ReportRequest;
import com.poshan.backend.dto.ReportResponse;
import com.poshan.backend.security.AuthContext;
import com.poshan.backend.service.ReportService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuthContext authContext;

    public ReportController(ReportService reportService, AuthContext authContext) {
        this.reportService = reportService;
        this.authContext = authContext;
    }

    @PostMapping
    public ReportResponse create(@RequestBody ReportRequest request) {
        ReportRequest securedRequest = new ReportRequest(
            request.memberId(),
            authContext.requireNutritionistId(),
            request.goal(),
            request.sessionsCompleted(),
            request.completion(),
            request.bmiChange(),
            request.sessionDate(),
            request.clinicalNote(),
            request.recommendations(),
            request.goalsMet()
        );
        return reportService.create(securedRequest);
    }

    @GetMapping("/nutritionist")
    public List<ReportResponse> getForNutritionist() {
        return reportService.getForNutritionist(authContext.requireNutritionistId());
    }
}

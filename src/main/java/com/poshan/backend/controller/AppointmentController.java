package com.poshan.backend.controller;

import com.poshan.backend.dto.AppointmentRequest;
import com.poshan.backend.dto.AppointmentResponse;
import com.poshan.backend.security.AuthContext;
import com.poshan.backend.service.AppointmentService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthContext authContext;

    public AppointmentController(AppointmentService appointmentService, AuthContext authContext) {
        this.appointmentService = appointmentService;
        this.authContext = authContext;
    }

    @PostMapping
    public AppointmentResponse create(@RequestBody AppointmentRequest request) {
        AppointmentRequest securedRequest = new AppointmentRequest(
            authContext.requireMemberId(),
            request.nutritionistId(),
            request.dateLabel(),
            request.timeLabel(),
            request.mode(),
            request.scheduledAt(),
            request.notes()
        );
        return appointmentService.create(securedRequest);
    }

    @GetMapping("/member")
    public List<AppointmentResponse> getForMember() {
        return appointmentService.getForMember(authContext.requireMemberId());
    }

    @GetMapping("/nutritionist")
    public List<AppointmentResponse> getForNutritionist(@RequestParam(required = false) String status) {
        return appointmentService.getForNutritionist(authContext.requireNutritionistId(), status);
    }
}

package com.poshan.backend.controller;

import com.poshan.backend.dto.PaymentRequest;
import com.poshan.backend.dto.PaymentResponse;
import com.poshan.backend.security.AuthContext;
import com.poshan.backend.service.PaymentService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthContext authContext;

    public PaymentController(PaymentService paymentService, AuthContext authContext) {
        this.paymentService = paymentService;
        this.authContext = authContext;
    }

    @PostMapping
    public PaymentResponse create(@RequestBody PaymentRequest request) {
        PaymentRequest securedRequest = new PaymentRequest(
            authContext.requireMemberId(),
            request.planId(),
            request.planLabel(),
            request.amount(),
            request.total()
        );
        return paymentService.create(securedRequest);
    }

    @GetMapping("/member")
    public List<PaymentResponse> getForMember() {
        return paymentService.getForMember(authContext.requireMemberId());
    }

    @GetMapping("/nutritionist")
    public List<PaymentResponse> getForNutritionist() {
        return paymentService.getForNutritionist(authContext.requireNutritionistId());
    }
}

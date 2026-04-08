package com.poshan.backend.service;

import com.poshan.backend.dto.PaymentRequest;
import com.poshan.backend.dto.PaymentResponse;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.Payment;
import com.poshan.backend.enums.PaymentStatus;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    public PaymentService(PaymentRepository paymentRepository, MemberRepository memberRepository) {
        this.paymentRepository = paymentRepository;
        this.memberRepository = memberRepository;
    }

    public PaymentResponse create(PaymentRequest request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        Payment payment = new Payment();
        payment.setMember(member);
        payment.setPlanId(request.planId());
        payment.setPlanLabel(request.planLabel());
        payment.setAmount(request.amount());
        payment.setTotal(request.total());
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        return toResponse(paymentRepository.save(payment));
    }

    public List<PaymentResponse> getForMember(Long memberId) {
        return paymentRepository.findAllByMemberIdOrderByPaidAtDesc(memberId).stream()
            .map(this::toResponse)
            .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getMember().getId(),
            payment.getPlanId(),
            payment.getPlanLabel(),
            payment.getAmount(),
            payment.getTotal(),
            payment.getTransactionId(),
            payment.getStatus().name(),
            payment.getPaidAt()
        );
    }
}

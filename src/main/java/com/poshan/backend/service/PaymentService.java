package com.poshan.backend.service;

import com.poshan.backend.dto.PaymentRequest;
import com.poshan.backend.dto.PaymentResponse;
import com.poshan.backend.entity.Appointment;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.MemberProfile;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.entity.Payment;
import com.poshan.backend.enums.PaymentStatus;
import com.poshan.backend.repository.AppointmentRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.MemberProfileRepository;
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
    private final MemberProfileRepository memberProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public PaymentService(
        PaymentRepository paymentRepository,
        MemberRepository memberRepository,
        MemberProfileRepository memberProfileRepository,
        AppointmentRepository appointmentRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.memberRepository = memberRepository;
        this.memberProfileRepository = memberProfileRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public PaymentResponse create(PaymentRequest request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        Nutritionist nutritionist = resolveNutritionist(member);

        Payment payment = new Payment();
        payment.setMember(member);
        payment.setNutritionist(nutritionist);
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

    public List<PaymentResponse> getForNutritionist(Long nutritionistId) {
        return paymentRepository.findAllByNutritionistIdOrderByPaidAtDesc(nutritionistId).stream()
            .map(this::toResponse)
            .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getMember().getId(),
            payment.getMember().getName(),
            payment.getNutritionist() != null ? payment.getNutritionist().getId() : null,
            payment.getNutritionist() != null ? payment.getNutritionist().getName() : null,
            payment.getPlanId(),
            payment.getPlanLabel(),
            payment.getAmount(),
            payment.getTotal(),
            payment.getTransactionId(),
            payment.getStatus().name(),
            payment.getPaidAt()
        );
    }

    private Nutritionist resolveNutritionist(Member member) {
        MemberProfile profile = memberProfileRepository.findByMemberId(member.getId()).orElse(null);

        if (profile != null && profile.getAssignedNutritionist() != null) {
            return profile.getAssignedNutritionist();
        }

        Appointment appointment = appointmentRepository.findFirstByMemberIdOrderByScheduledAtDesc(member.getId()).orElse(null);
        return appointment != null ? appointment.getNutritionist() : null;
    }
}

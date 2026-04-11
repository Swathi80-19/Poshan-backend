package com.poshan.backend.service;

import com.poshan.backend.dto.AppointmentRequest;
import com.poshan.backend.dto.AppointmentResponse;
import com.poshan.backend.entity.Appointment;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.MemberProfile;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.enums.AppointmentMode;
import com.poshan.backend.enums.AppointmentStatus;
import com.poshan.backend.repository.AppointmentRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.MemberProfileRepository;
import com.poshan.backend.repository.NutritionistRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final NutritionistRepository nutritionistRepository;

    public AppointmentService(
        AppointmentRepository appointmentRepository,
        MemberRepository memberRepository,
        MemberProfileRepository memberProfileRepository,
        NutritionistRepository nutritionistRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.memberRepository = memberRepository;
        this.memberProfileRepository = memberProfileRepository;
        this.nutritionistRepository = nutritionistRepository;
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        Nutritionist nutritionist = nutritionistRepository.findById(request.nutritionistId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist not found"));

        Appointment appointment = new Appointment();
        appointment.setMember(member);
        appointment.setNutritionist(nutritionist);
        appointment.setDateLabel(request.dateLabel());
        appointment.setTimeLabel(request.timeLabel());
        appointment.setScheduledAt(request.scheduledAt());
        appointment.setMode(parseMode(request.mode()));
        appointment.setStatus(AppointmentStatus.UPCOMING);
        appointment.setNotes(request.notes());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        assignNutritionistToMemberProfile(member, nutritionist);
        return toResponse(savedAppointment);
    }

    public List<AppointmentResponse> getForNutritionist(Long nutritionistId, String status) {
        List<Appointment> items = status == null || status.isBlank()
            ? appointmentRepository.findAllByNutritionistIdOrderByScheduledAtAsc(nutritionistId)
            : appointmentRepository.findAllByNutritionistIdAndStatusOrderByScheduledAtAsc(
                nutritionistId,
                AppointmentStatus.valueOf(status.toUpperCase())
            );
        return items.stream().map(this::toResponse).toList();
    }

    public List<AppointmentResponse> getForMember(Long memberId) {
        return appointmentRepository.findAllByMemberIdOrderByScheduledAtAsc(memberId).stream()
            .map(this::toResponse)
            .toList();
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getMember().getId(),
            appointment.getMember().getName(),
            appointment.getNutritionist().getId(),
            appointment.getNutritionist().getName(),
            appointment.getDateLabel(),
            appointment.getTimeLabel(),
            appointment.getMode().name(),
            appointment.getStatus().name(),
            appointment.getScheduledAt(),
            appointment.getNotes()
        );
    }

    private AppointmentMode parseMode(String value) {
        if (value == null || value.isBlank()) {
            return AppointmentMode.VIDEO;
        }

        String normalized = value.trim()
            .replace('-', '_')
            .replace(' ', '_')
            .toUpperCase();

        if (normalized.contains("CHAT")) {
            return AppointmentMode.CHAT;
        }

        if (normalized.contains("PERSON")) {
            return AppointmentMode.IN_PERSON;
        }

        return AppointmentMode.VIDEO;
    }

    private void assignNutritionistToMemberProfile(Member member, Nutritionist nutritionist) {
        MemberProfile profile = memberProfileRepository.findByMemberId(member.getId()).orElseGet(MemberProfile::new);
        profile.setMember(member);
        profile.setAssignedNutritionist(nutritionist);
        memberProfileRepository.save(profile);
    }
}

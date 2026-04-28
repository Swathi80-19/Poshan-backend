package com.poshan.backend.service;

import com.poshan.backend.dto.DashboardResponse;
import com.poshan.backend.dto.NutritionistProfileRequest;
import com.poshan.backend.dto.NutritionistProfileResponse;
import com.poshan.backend.entity.Appointment;
import com.poshan.backend.dto.NutritionistPatientResponse;
import com.poshan.backend.dto.NutritionistSummaryResponse;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.MemberProfile;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.repository.ActivityLogRepository;
import com.poshan.backend.repository.AppointmentRepository;
import com.poshan.backend.repository.FoodLogRepository;
import com.poshan.backend.repository.MemberProfileRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.NutritionistRepository;
import com.poshan.backend.repository.ReportRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NutritionistService {

    private final NutritionistRepository nutritionistRepository;
    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final FoodLogRepository foodLogRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReportRepository reportRepository;

    public NutritionistService(
        NutritionistRepository nutritionistRepository,
        MemberRepository memberRepository,
        MemberProfileRepository memberProfileRepository,
        FoodLogRepository foodLogRepository,
        ActivityLogRepository activityLogRepository,
        AppointmentRepository appointmentRepository,
        ReportRepository reportRepository
    ) {
        this.nutritionistRepository = nutritionistRepository;
        this.memberRepository = memberRepository;
        this.memberProfileRepository = memberProfileRepository;
        this.foodLogRepository = foodLogRepository;
        this.activityLogRepository = activityLogRepository;
        this.appointmentRepository = appointmentRepository;
        this.reportRepository = reportRepository;
    }

    public List<NutritionistSummaryResponse> getNutritionists() {
        return nutritionistRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(nutritionist -> new NutritionistSummaryResponse(
                nutritionist.getId(),
                nutritionist.getName(),
                nutritionist.getUsername(),
                nutritionist.getEmail(),
                nutritionist.getSpecialization(),
                nutritionist.getExperience()
            ))
            .toList();
    }

    public List<NutritionistPatientResponse> getPatients(Long nutritionistId) {
        List<Appointment> appointments = appointmentRepository.findAllByNutritionistIdOrderByScheduledAtAsc(nutritionistId);
        Set<Long> memberIds = appointments.stream()
            .map(appointment -> appointment.getMember().getId())
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        memberIds.addAll(memberProfileRepository.findAllByAssignedNutritionistId(nutritionistId).stream()
            .map(profile -> profile.getMember().getId())
            .collect(Collectors.toCollection(java.util.LinkedHashSet::new)));

        return memberIds.stream()
            .map(memberId -> memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found")))
            .filter(member -> hasRealData(member, nutritionistId))
            .map(member -> toPatientResponse(member, appointments))
            .toList();
    }

    public DashboardResponse getNutritionistDashboard(Long nutritionistId) {
        Nutritionist nutritionist = nutritionistRepository.findById(nutritionistId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist not found"));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("nutritionist", nutritionist.getName());
        List<Appointment> appointments = appointmentRepository.findAllByNutritionistIdOrderByScheduledAtAsc(nutritionistId);
        List<NutritionistPatientResponse> patients = getPatients(nutritionistId);
        summary.put("patients", patients.size());
        summary.put("appointments", appointments.size());
        summary.put("reports", reportRepository.findAllByNutritionistIdOrderBySessionDateDesc(nutritionistId).size());
        return new DashboardResponse(summary, patients);
    }

    public NutritionistProfileResponse getProfile(Long nutritionistId) {
        return toProfileResponse(getNutritionist(nutritionistId));
    }

    public NutritionistProfileResponse updateProfile(Long nutritionistId, NutritionistProfileRequest request) {
        Nutritionist nutritionist = getNutritionist(nutritionistId);
        nutritionist.setAge(request.age());
        nutritionist.setBio(normalizeOptionalValue(request.bio()));
        return toProfileResponse(nutritionistRepository.save(nutritionist));
    }

    private boolean hasRealData(Member member, Long nutritionistId) {
        MemberProfile profile = memberProfileRepository.findByMemberId(member.getId()).orElse(null);
        return profile != null && profile.getAssignedNutritionist() != null && profile.getAssignedNutritionist().getId().equals(nutritionistId)
            || !foodLogRepository.findAllByMemberIdOrderByLoggedAtDesc(member.getId()).isEmpty()
            || !activityLogRepository.findAllByMemberIdOrderByLoggedAtDesc(member.getId()).isEmpty()
            || !appointmentRepository.findAllByMemberIdOrderByScheduledAtAsc(member.getId()).isEmpty();
    }

    private NutritionistPatientResponse toPatientResponse(Member member, List<Appointment> appointments) {
        MemberProfile profile = memberProfileRepository.findByMemberId(member.getId()).orElse(null);
        int sessions = (int) appointments.stream()
            .filter(appointment -> appointment.getMember().getId().equals(member.getId()))
            .count();
        return new NutritionistPatientResponse(
            member.getId(),
            member.getName(),
            profile != null ? profile.getAge() : null,
            profile != null ? profile.getGoalFocus() : null,
            sessions,
            foodLogRepository.findAllByMemberIdOrderByLoggedAtDesc(member.getId()).size(),
            activityLogRepository.findAllByMemberIdOrderByLoggedAtDesc(member.getId()).size()
        );
    }

    private Nutritionist getNutritionist(Long nutritionistId) {
        return nutritionistRepository.findById(nutritionistId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist not found"));
    }

    private NutritionistProfileResponse toProfileResponse(Nutritionist nutritionist) {
        return new NutritionistProfileResponse(
            nutritionist.getId(),
            nutritionist.getName(),
            nutritionist.getUsername(),
            nutritionist.getEmail(),
            nutritionist.getPhone(),
            nutritionist.getSpecialization(),
            nutritionist.getExperience(),
            nutritionist.getAge(),
            nutritionist.getBio(),
            nutritionist.getAge() != null
        );
    }

    private String normalizeOptionalValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

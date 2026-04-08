package com.poshan.backend.service;

import com.poshan.backend.dto.DashboardResponse;
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
        return nutritionistRepository.findAll().stream()
            .map(nutritionist -> new NutritionistSummaryResponse(
                nutritionist.getId(),
                nutritionist.getName(),
                nutritionist.getUsername(),
                nutritionist.getEmail(),
                nutritionist.getSpecialization()
            ))
            .toList();
    }

    public List<NutritionistPatientResponse> getPatients() {
        return memberRepository.findAll().stream()
            .filter(this::hasRealData)
            .map(this::toPatientResponse)
            .toList();
    }

    public DashboardResponse getNutritionistDashboard(Long nutritionistId) {
        Nutritionist nutritionist = nutritionistRepository.findById(nutritionistId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist not found"));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("nutritionist", nutritionist.getName());
        summary.put("patients", getPatients().size());
        summary.put("appointments", appointmentRepository.findAllByNutritionistIdOrderByScheduledAtAsc(nutritionistId).size());
        summary.put("reports", reportRepository.findAllByNutritionistIdOrderBySessionDateDesc(nutritionistId).size());
        return new DashboardResponse(summary, getPatients());
    }

    private boolean hasRealData(Member member) {
        return memberProfileRepository.findByMemberId(member.getId()).isPresent()
            || !foodLogRepository.findAllByMemberIdOrderByLoggedAtDesc(member.getId()).isEmpty()
            || !activityLogRepository.findAllByMemberIdOrderByLoggedAtDesc(member.getId()).isEmpty()
            || !appointmentRepository.findAllByMemberIdOrderByScheduledAtAsc(member.getId()).isEmpty();
    }

    private NutritionistPatientResponse toPatientResponse(Member member) {
        MemberProfile profile = memberProfileRepository.findByMemberId(member.getId()).orElse(null);
        return new NutritionistPatientResponse(
            member.getId(),
            member.getName(),
            profile != null ? profile.getAge() : null,
            profile != null ? profile.getGoalFocus() : null,
            appointmentRepository.findAllByMemberIdOrderByScheduledAtAsc(member.getId()).size(),
            foodLogRepository.findAllByMemberIdOrderByLoggedAtDesc(member.getId()).size(),
            activityLogRepository.findAllByMemberIdOrderByLoggedAtDesc(member.getId()).size()
        );
    }
}

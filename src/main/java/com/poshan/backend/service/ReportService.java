package com.poshan.backend.service;

import com.poshan.backend.dto.ReportRequest;
import com.poshan.backend.dto.ReportResponse;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.entity.Report;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.NutritionistRepository;
import com.poshan.backend.repository.ReportRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final NutritionistRepository nutritionistRepository;

    public ReportService(
        ReportRepository reportRepository,
        MemberRepository memberRepository,
        NutritionistRepository nutritionistRepository
    ) {
        this.reportRepository = reportRepository;
        this.memberRepository = memberRepository;
        this.nutritionistRepository = nutritionistRepository;
    }

    public ReportResponse create(ReportRequest request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        Nutritionist nutritionist = nutritionistRepository.findById(request.nutritionistId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist not found"));

        Report report = new Report();
        report.setMember(member);
        report.setNutritionist(nutritionist);
        report.setGoal(request.goal());
        report.setSessionsCompleted(request.sessionsCompleted());
        report.setCompletion(request.completion());
        report.setBmiChange(request.bmiChange());
        report.setSessionDate(request.sessionDate());
        report.setClinicalNote(request.clinicalNote());
        report.setRecommendations(request.recommendations());
        report.setGoalsMet(request.goalsMet() != null ? request.goalsMet() : List.of());
        return toResponse(reportRepository.save(report));
    }

    public List<ReportResponse> getForNutritionist(Long nutritionistId) {
        return reportRepository.findAllByNutritionistIdOrderBySessionDateDesc(nutritionistId).stream()
            .map(this::toResponse)
            .toList();
    }

    private ReportResponse toResponse(Report report) {
        return new ReportResponse(
            report.getId(),
            report.getMember().getId(),
            report.getMember().getName(),
            report.getNutritionist().getId(),
            report.getNutritionist().getName(),
            report.getGoal(),
            report.getSessionsCompleted(),
            report.getCompletion(),
            report.getBmiChange(),
            report.getSessionDate(),
            report.getClinicalNote(),
            report.getRecommendations(),
            report.getGoalsMet()
        );
    }
}

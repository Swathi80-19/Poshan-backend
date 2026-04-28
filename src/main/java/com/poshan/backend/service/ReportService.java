package com.poshan.backend.service;

import com.poshan.backend.dto.ReportRequest;
import com.poshan.backend.dto.ReportResponse;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.entity.Report;
import java.util.Base64;
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
        report.setAttachmentFileName(normalizeOptionalValue(request.attachmentFileName()));
        report.setAttachmentContentType(normalizeOptionalValue(request.attachmentContentType()));
        report.setAttachmentSize(request.attachmentSize());
        report.setAttachmentData(decodeAttachment(request.attachmentBase64()));
        return toResponse(reportRepository.save(report));
    }

    public List<ReportResponse> getForNutritionist(Long nutritionistId) {
        return reportRepository.findAllByNutritionistIdOrderBySessionDateDesc(nutritionistId).stream()
            .map(this::toResponse)
            .toList();
    }

    public Report getAttachment(Long nutritionistId, Long reportId) {
        Report report = reportRepository.findByIdAndNutritionistId(reportId, nutritionistId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        if (report.getAttachmentData() == null || report.getAttachmentData().length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No file is attached to this report.");
        }

        return report;
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
            report.getGoalsMet(),
            report.getAttachmentData() != null && report.getAttachmentData().length > 0,
            report.getAttachmentFileName(),
            report.getAttachmentContentType(),
            report.getAttachmentSize()
        );
    }

    private byte[] decodeAttachment(String attachmentBase64) {
        if (attachmentBase64 == null || attachmentBase64.isBlank()) {
            return null;
        }

        try {
            return Base64.getDecoder().decode(attachmentBase64);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded report file could not be read.");
        }
    }

    private String normalizeOptionalValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

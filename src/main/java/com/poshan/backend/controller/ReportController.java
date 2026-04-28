package com.poshan.backend.controller;

import com.poshan.backend.dto.ReportRequest;
import com.poshan.backend.dto.ReportResponse;
import com.poshan.backend.entity.Report;
import com.poshan.backend.security.AuthContext;
import com.poshan.backend.service.ReportService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            request.goalsMet(),
            request.attachmentFileName(),
            request.attachmentContentType(),
            request.attachmentSize(),
            request.attachmentBase64()
        );
        return reportService.create(securedRequest);
    }

    @GetMapping("/nutritionist")
    public List<ReportResponse> getForNutritionist() {
        return reportService.getForNutritionist(authContext.requireNutritionistId());
    }

    @GetMapping("/{reportId}/attachment")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long reportId) {
        Report report = reportService.getAttachment(authContext.requireNutritionistId(), reportId);
        String contentType = report.getAttachmentContentType() != null ? report.getAttachmentContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String fileName = report.getAttachmentFileName() != null ? report.getAttachmentFileName() : "report-file";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName.replace("\"", "") + "\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(report.getAttachmentData());
    }
}

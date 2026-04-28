package com.poshan.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record ReportRequest(
    Long memberId,
    Long nutritionistId,
    String goal,
    Integer sessionsCompleted,
    Integer completion,
    String bmiChange,
    LocalDate sessionDate,
    String clinicalNote,
    String recommendations,
    List<String> goalsMet,
    String attachmentFileName,
    String attachmentContentType,
    Long attachmentSize,
    String attachmentBase64
) {
}

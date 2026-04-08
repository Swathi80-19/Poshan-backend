package com.poshan.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record ReportResponse(
    Long id,
    Long memberId,
    String memberName,
    Long nutritionistId,
    String nutritionistName,
    String goal,
    Integer sessionsCompleted,
    Integer completion,
    String bmiChange,
    LocalDate sessionDate,
    String clinicalNote,
    String recommendations,
    List<String> goalsMet
) {
}

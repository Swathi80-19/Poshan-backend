package com.poshan.backend.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
    Map<String, Object> summary,
    List<?> items
) {
}

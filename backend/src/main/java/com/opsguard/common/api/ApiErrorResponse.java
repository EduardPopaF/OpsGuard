package com.opsguard.common.api;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String code,
        String message,
        Map<String, String> errors,
        String path,
        OffsetDateTime timestamp
) {
}
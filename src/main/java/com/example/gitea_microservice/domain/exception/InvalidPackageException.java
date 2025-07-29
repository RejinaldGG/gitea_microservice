package com.example.gitea_microservice.domain.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;

@Getter
public class InvalidPackageException extends ResponseStatusException {
    private final PackageErrorType errorType;
    private final Map<String, Object> details;

    public InvalidPackageException(PackageErrorType errorType, HttpStatusCode statusCode, String message) {
        this(errorType, statusCode, message, null);
    }

    public InvalidPackageException(PackageErrorType errorType, HttpStatusCode statusCode, String message, Map<String, Object> details) {
        super(statusCode, message);
        this.errorType = errorType;
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
    }

    public InvalidPackageException withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }

    public InvalidPackageException withDetails(Map<String, Object> details) {
        if (details != null) {
            this.details.putAll(details);
        }
        return this;
    }

    public Map<String, Object> getErrorResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("errorType", errorType);
        response.put("status", getStatusCode().value());
        response.put("message", getMessage());
        response.put("timestamp", Instant.now());
        if (!details.isEmpty()) {
            response.put("details", new LinkedHashMap<>(details));
        }
        return response;
    }
}
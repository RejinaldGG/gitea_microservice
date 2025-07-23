package com.example.gitea_microservice.domain.exception;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class InvalidPackageException extends RuntimeException {
    private final PackageErrorType errorType;
    private final Map<String, Object> details;

    public InvalidPackageException(PackageErrorType errorType, String message) {
        this(errorType, message, null);
    }

    public InvalidPackageException(PackageErrorType errorType, String message, Map<String, Object> details) {
        super(message);
        this.errorType = errorType;
        this.details = details != null ? details : new HashMap<>();
    }

    public InvalidPackageException withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
}
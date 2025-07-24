package com.example.gitea_microservice.infrastructure.ports;

import com.example.gitea_microservice.domain.models.PackageManager;

public interface PackageVersionExtractor {
    String extract(String filename);

    boolean supports(PackageManager manager);
}

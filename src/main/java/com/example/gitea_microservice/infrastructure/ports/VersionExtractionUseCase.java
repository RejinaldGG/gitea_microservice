package com.example.gitea_microservice.infrastructure.ports;

import com.example.gitea_microservice.domain.models.PackageManager;

public interface VersionExtractionUseCase {
    String extractVersion(PackageManager manager, String filename);
}

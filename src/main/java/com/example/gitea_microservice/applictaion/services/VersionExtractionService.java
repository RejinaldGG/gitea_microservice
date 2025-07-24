package com.example.gitea_microservice.applictaion.services;

import java.util.List;
import org.springframework.stereotype.Service;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageVersionExtractor;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VersionExtractionService implements VersionExtractionUseCase{
     private final List<PackageVersionExtractor> extractors;

    @Override
    public String extractVersion(PackageManager manager, String filename) {
        return extractors.stream()
            .filter(e -> e.supports(manager))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No extractor defined for: " + manager))
            .extract(filename);
    }
}


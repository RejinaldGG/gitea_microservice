package com.example.gitea_microservice.applictaion.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.adapters.VersionExtractors.AlpineVersionExtractor;
import com.example.gitea_microservice.infrastructure.adapters.VersionExtractors.ArchVersionExtractor;
import com.example.gitea_microservice.infrastructure.ports.PackageVersionExtractor;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

@Service
public class VersionExtractionService implements VersionExtractionUseCase{
    private final Map<PackageManager, PackageVersionExtractor> extractors = new HashMap<>();

    public VersionExtractionService() {
        extractors.put(PackageManager.ALPINE, new AlpineVersionExtractor());
        extractors.put(PackageManager.ARCH, new ArchVersionExtractor());
    }

    @Override
    public String extractVersion(PackageManager manager, String filename) {
        PackageVersionExtractor extractor = extractors.get(manager);
        if (extractor == null) {
            throw new RuntimeException("No extractor defined for: " + manager);
        }
        return extractor.extract(filename);
    }
}


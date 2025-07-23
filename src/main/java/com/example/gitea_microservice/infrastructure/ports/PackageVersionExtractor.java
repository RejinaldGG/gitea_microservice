package com.example.gitea_microservice.infrastructure.ports;

public interface PackageVersionExtractor {
    String extract(String filename);
}

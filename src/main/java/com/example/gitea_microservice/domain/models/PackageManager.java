package com.example.gitea_microservice.domain.models;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PackageManager {
    ALPINE("Alpine", PublishType.HTTP),
    ARCH("Arch", PublishType.HTTP),
    CARGO("Cargo", PublishType.MANAGER),
    CHEF("Chef", PublishType.MANAGER),
    COMPOSER("Composer", PublishType.HTTP),
    CONAN("Conan", PublishType.MANAGER),
    CONDA("Conda", PublishType.HTTP),
    CONTAINER("Container", PublishType.MANAGER),
    CRAN("CRAN", PublishType.HTTP),
    DEBIAN("Debian", PublishType.HTTP),
    GENERIC("Generic", PublishType.MANAGER),
    GO("Go", PublishType.HTTP),
    HELM("Helm", PublishType.MANAGER),
    NUGET("NuGet", PublishType.MANAGER),
    PUB("Pub", PublishType.MANAGER),
    PYPI("PyPI", PublishType.MANAGER),
    RPM("RPM", PublishType.HTTP),
    RUBYGEMS("RubyGems", PublishType.HTTP),
    SWIFT("Swift", PublishType.MANAGER);

    private final String displayName;
    private final PublishType publishType;

    
    PackageManager(String displayName, PublishType publishType) {
        this.displayName = displayName;
        this.publishType = publishType;
    }

    public PublishType getPublishType(){
        return publishType;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    @JsonCreator
    public static PackageManager fromString(String value) {
        try {
            return PackageManager.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Unknown package manager: %s. Supported values: %s",
                    value, Arrays.toString(values()))
            );
        }
    }

    public String toUrlName() {
        return name().toLowerCase();
    }
   
}

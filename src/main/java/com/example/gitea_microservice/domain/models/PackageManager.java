package com.example.gitea_microservice.domain.models;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PackageManager {
    ALPINE("Alpine"),
    ARCH("Arch"),
    CARGO("Cargo"),
    CHEF("Chef"),
    COMPOSER("Composer"),
    CONAN("Conan"),
    CONDA("Conda"),
    CONTAINER("Container"),
    CRAN("CRAN"),
    DEBIAN("Debian"),
    GENERIC("Generic"),
    GO("Go"),
    HELM("Helm"),
    NUGET("NuGet"),
    PUB("Pub"),
    PYPI("PyPI"),
    RPM("RPM"),
    RUBYGEMS("RubyGems"),
    SWIFT("Swift");

    private final String displayName;

    PackageManager(String displayName) {
        this.displayName = displayName;
    }
    @JsonValue
    public String getdisplayName() {
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

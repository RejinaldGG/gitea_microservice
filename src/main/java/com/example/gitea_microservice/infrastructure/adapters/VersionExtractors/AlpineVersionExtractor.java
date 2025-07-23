package com.example.gitea_microservice.infrastructure.adapters.VersionExtractors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.infrastructure.ports.PackageVersionExtractor;

public class AlpineVersionExtractor implements PackageVersionExtractor {
    private static final Pattern pattern = Pattern.compile("-((\\d+(?:\\.\\d+)*)(?:-r\\d+))\\.apk$");

    @Override
    public String extract(String filename) {
        if (filename != null) {


            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                String version = matcher.group(1); 
                return version;
            }
        }
        throw new InvalidPackageException(PackageErrorType.INVALID_FORMAT, "Could not extract version from filename: " + filename);
}
}


package com.example.gitea_microservice.infrastructure.adapters.VersionExtractors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageVersionExtractor;

@Component
public class DebianVersionExtractor implements PackageVersionExtractor {
    private static final Pattern pattern = Pattern.compile(
        "^.*_" +                   
        "([^_]+)" +                
        "_" +                      
        "(?:amd64|arm64|i386|all)" + 
        "\\.deb$",                 
        Pattern.CASE_INSENSITIVE
    );
    private final PackageManager supportedManager = PackageManager.DEBIAN;
        @Override
        public boolean supports(PackageManager manager) {
            return manager == supportedManager;
        }
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

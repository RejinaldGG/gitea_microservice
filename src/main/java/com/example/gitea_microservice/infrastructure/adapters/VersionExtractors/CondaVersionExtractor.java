package com.example.gitea_microservice.infrastructure.adapters.VersionExtractors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageVersionExtractor;
@Component
public class CondaVersionExtractor implements PackageVersionExtractor {
    private static final Pattern pattern = Pattern.compile(
   "-" +                                  
    "(\\d+(?:\\.\\d+)*)" +                 
    "(?:-(\\d+))?" +                       
    "(?:-[a-zA-Z0-9_]+)?" +                
    "\\.(?:tar\\.bz2|conda)" +             
    "$",                                   
    Pattern.CASE_INSENSITIVE
);
    private final PackageManager supportedManager = PackageManager.CONDA;
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
        throw new InvalidPackageException(PackageErrorType.INVALID_FORMAT, HttpStatus.UNPROCESSABLE_ENTITY, "Could not extract version from filename: " + filename);
}
}

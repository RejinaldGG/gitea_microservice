package com.example.gitea_microservice.domain.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitPackage {

    private PackageManager manager;
    private String name;
    private String version;
    private byte[] content;

}

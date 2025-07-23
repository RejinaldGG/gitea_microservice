package com.example.gitea_microservice.infrastructure.ports;

import com.example.gitea_microservice.domain.models.GitPackage;

public interface GiteaClientPort {

    void uploadPackage(GitPackage pkg);
}

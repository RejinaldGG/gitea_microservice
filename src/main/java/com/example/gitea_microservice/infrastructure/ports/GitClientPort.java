package com.example.gitea_microservice.infrastructure.ports;

import com.example.gitea_microservice.domain.models.GitPackage;

public interface GitClientPort {

    void uploadAlpinePackage(GitPackage pkg);
    void uploadComposerPackage(GitPackage pkg);
    void uploadCargoPackage(GitPackage pkg);
    void uploadArchPackage(GitPackage pkg);
    void uploadCondaPackage(GitPackage pkg);
    void uploadCranPackage(GitPackage pkg);
    void uploadDebianPackage(GitPackage pkg);
    void uploadGoPackage(GitPackage pkg);
    void uploadRpmPackage(GitPackage pkg);
    void uploadRubyGemsPackage(GitPackage pkg);
}

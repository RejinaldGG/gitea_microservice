package com.example.gitea_microservice.infrastructure.adapters;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.UploadPackageUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {
    private final UploadPackageUseCase uploadUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
        @RequestParam String manager,
        @RequestPart MultipartFile file
    ) {
        uploadUseCase.uploadPackage(PackageManager.fromString(manager), file);
        return ResponseEntity.accepted().build();
    }
}
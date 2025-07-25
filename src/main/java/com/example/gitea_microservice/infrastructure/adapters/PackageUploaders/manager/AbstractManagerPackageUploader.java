package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import com.example.gitea_microservice.infrastructure.ports.UploadPackagePort;

public abstract class AbstractManagerPackageUploader implements UploadPackagePort{

    private final PackageValidatorUseCase packageValidator;
    public AbstractManagerPackageUploader(
                               PackageValidatorUseCase packageValidator) {
        this.packageValidator = packageValidator;
    }
    @Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
        packageValidator.validate(manager, file);
    try {

        Path tempDir = createTempDir(manager);
        Path pkgFile = writePackageFile(tempDir, file);
        unpack(pkgFile, tempDir);
        runUpload(tempDir);
        deleteTempDir(tempDir);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException("Failed to publish "+ manager.getDisplayName() +" package", e);
    }
    }
    
    protected void deleteTempDir(Path dir) throws IOException {
    if (!Files.exists(dir)) return;

    try (Stream<Path> walk = Files.walk(dir)) {
        walk.sorted(Comparator.reverseOrder()) 
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    System.err.printf("Failed to delete %s%n", path);
                }
            });
    }
    }
    
    protected Path createTempDir(PackageManager manager) throws IOException
    {
        return Files.createTempDirectory(manager.getDisplayName() + "-");
        
    }
    
    protected Path writePackageFile(Path tempDir, MultipartFile file) throws IllegalStateException, IOException
    {
        Path pkgFile = tempDir.resolve(file.getOriginalFilename());
        file.transferTo(pkgFile.toFile());
        return pkgFile;
    } 

    protected String runCommand(Path packageDir, List<String> command) {


        ProcessBuilder pb = new ProcessBuilder()
                .command(command)
                .directory(packageDir.toFile())
                .redirectErrorStream(true);

        StringBuilder output = new StringBuilder();
        try {
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Command failed with exit code " + exitCode + "\nOutput:\n" + output);
            }

            return output.toString();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to run command: " + e.getMessage(), e);
        }
    } 

    protected abstract void unpack(Path crateFile, Path outputDir) throws IOException;
    protected abstract void runUpload(Path cookbookDir) throws IOException, InterruptedException;
} 

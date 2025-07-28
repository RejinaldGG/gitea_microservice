package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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

    protected void validate(PackageManager manager, MultipartFile file)
    {
                packageValidator.validate(manager, file);
    }
    @Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
        validate(manager, file);
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
    protected void unpack(Path packageFile, Path outputDir) throws IOException {
        try (InputStream fis = Files.newInputStream(packageFile);
            GZIPInputStream gzipIn = new GZIPInputStream(fis);
            TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

            TarArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                Path outPath = outputDir.resolve(entry.getName()).normalize();
                if (!outPath.startsWith(outputDir)) {
                    throw new IOException("Unsafe path: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (OutputStream out = Files.newOutputStream(outPath)) {
                        IOUtils.copy(tarIn, out);
                    }
                }
            }
        }
    }
    protected abstract void runUpload(Path packageDir) throws IOException, InterruptedException;
} 

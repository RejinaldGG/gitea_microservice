package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.config.GiteaConfig;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConanPackageUploader extends AbstractManagerPackageUploader {
    private final VersionExtractionUseCase versionExtractor;
    private final PackageValidatorUseCase packageValidator;
    private final GiteaConfig giteaConfig;
    public ConanPackageUploader(
        PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig,VersionExtractionUseCase versionExtractor) {
            super(packageValidator);
            this.versionExtractor = versionExtractor;
            this.packageValidator = packageValidator;
            this.giteaConfig = giteaConfig;

    }
    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.CONAN;
    }

    private Path findProjectDir(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root, 2)) {
            return paths
                .filter(p -> p.getFileName().toString().equals("conanfile.py"))
                .findFirst()
                .orElseThrow(() -> new IOException("conanfile.py not found"))
                .getParent();
        }
    }
    @Override
    protected void unpack(Path crateFile, Path outputDir) throws IOException {
        try (InputStream fis = Files.newInputStream(crateFile);
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

    private String extractConanAttribute(Path conanfile, String attribute) throws IOException {
        return Files.readAllLines(conanfile).stream()
            .map(String::trim)
            .filter(line -> line.startsWith(attribute))
            .map(line -> line.split("="))
            .filter(parts -> parts.length == 2)
            .map(parts -> parts[1].trim().replaceAll("['\"]", ""))
            .findFirst()
            .orElseThrow(() -> new InvalidPackageException(PackageErrorType.INVALID_FORMAT, "Could not find '" + attribute + "' in conanfile.py"));
    }

    private String extractName(Path conanfile) throws IOException {
        return extractConanAttribute(conanfile, "name");
    }

    private String extractVersion(Path conanfile) throws IOException {
        return extractConanAttribute(conanfile, "version");
    }

    private String runCommand(Path conanDir, List<String> command) {


        ProcessBuilder pb = new ProcessBuilder()
                .command(command)
                .directory(conanDir.toFile())
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
                throw new RuntimeException("Conan command failed with exit code " + exitCode + "\nOutput:\n" + output);
            }

            return output.toString();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to run Conan command: " + e.getMessage(), e);
        }
    }

    @Override
    protected void runUpload(Path tempDir) throws IOException, InterruptedException {
        Path conanDir = findProjectDir(tempDir);
        Path metadata = conanDir.resolve("conanfile.py");
        String name = extractName(metadata);
        String version = extractVersion(metadata);
        
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "remote", "add", giteaConfig.getOwner(), giteaConfig.getUrl()+"/api/packages/"+giteaConfig.getOwner()+"/conan")));
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "remote", "login", giteaConfig.getOwner(), giteaConfig.getOwner(), "-p", "giteagitea")));
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "profile", "detect", "--force")));
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "create", ".", "--name=" + name, "--version=" + version)));
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "upload", name+"/"+version,"--remote="+giteaConfig.getOwner())));
        
}
}
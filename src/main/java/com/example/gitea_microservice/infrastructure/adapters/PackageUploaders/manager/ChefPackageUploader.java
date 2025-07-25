package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class ChefPackageUploader extends AbstractManagerPackageUploader {
    private final PackageValidatorUseCase packageValidator;
    private final GiteaConfig giteaConfig;
    public ChefPackageUploader(
        PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig) {
            super(packageValidator);
            this.packageValidator = packageValidator;
            this.giteaConfig = giteaConfig;

    }
    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.CHEF;
    }
    
    private Path findProjectDir(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root, 2)) {
            return paths
                .filter(p -> p.getFileName().toString().equals("metadata.rb"))
                .findFirst()
                .orElseThrow(() -> new IOException("metadata.rb not found"))
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

    private String extractName(Path metadataPath) throws InvalidPackageException, IOException{
    String cookbookName = Files.readAllLines(metadataPath).stream()
        .map(String::trim)
        .filter(line -> line.startsWith("name"))
        .map(line -> line.split("\\s+"))
        .filter(parts -> parts.length >= 2)
        .map(parts -> parts[1].replaceAll("['\"]", ""))
        .findFirst()
        .orElseThrow(() -> new InvalidPackageException(PackageErrorType.INVALID_FORMAT, "Could not find 'name' in metadata.rb"));
        if (cookbookName == null) {
            new InvalidPackageException(PackageErrorType.INVALID_FORMAT, "Could not find 'name' in metadata.rb");
        }
        return cookbookName;
    }
    
    private void createConfig(Path configPath, Path cookBookPath) throws IOException
    {
    String configContent = """
                cookbook_path ["%s"]
                node_name "%s"
                client_key "/root/.chef/%s.priv"
                knife[:supermarket_site] = "%s/api/packages/%s/chef"
                """.formatted(
                    cookBookPath.toAbsolutePath().toString(),  
                    giteaConfig.getOwner(),
                    giteaConfig.getOwner(),
                    giteaConfig.getUrl(),
                    giteaConfig.getOwner()
                );

            Files.writeString(configPath, configContent);
    }
    @Override
    protected void runUpload(Path tempDir) throws IOException, InterruptedException {
        Path cookbookDir = findProjectDir(tempDir);
        Path metadata = cookbookDir.resolve("metadata.rb");
        String cookBookName = extractName(metadata);
        Path parentDir = cookbookDir.getParent();
        Path configPath = parentDir.resolve("config.rb");
        createConfig(configPath, parentDir);
       
        ProcessBuilder pb = new ProcessBuilder()
            .command("knife", "supermarket", "share", cookBookName, "-c", configPath.toString())
            .directory(cookbookDir.toFile()) 
            .redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        log.info("Chef (knife) publish output:\n{}", output);

        if (exitCode != 0) {
            throw new RuntimeException("Chef publish failed with exit code " + exitCode);
        }
    }
        
}
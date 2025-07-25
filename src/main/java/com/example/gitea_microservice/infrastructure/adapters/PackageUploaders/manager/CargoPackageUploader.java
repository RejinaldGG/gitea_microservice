package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.domain.models.PublishType;
import com.example.gitea_microservice.infrastructure.config.GiteaConfig;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class CargoPackageUploader extends AbstractManagerPackageUploader {
    private final GiteaConfig giteaConfig;
    public CargoPackageUploader(
        PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig) {
            super(packageValidator);
            this.giteaConfig = giteaConfig;
    }
    @Override
    public boolean supports(PackageManager manager) {
        return manager.getPublishType() == PublishType.MANAGER && manager == PackageManager.CARGO;
    }

    private void writeCargoConfig() throws IOException {
        Path cargoDir = Paths.get("/root/.cargo");
        if (!Files.exists(cargoDir)) {
            Files.createDirectories(cargoDir);
        }

        String configToml = """
            [registry]
            default = "gitea"

            [registries.gitea]
            index = "%s/%s/_cargo-index.git" # Git

            [net]
            git-fetch-with-cli = true
            """.formatted(giteaConfig.getUrl(), giteaConfig.getOwner());
        Files.writeString(cargoDir.resolve("config.toml"), configToml, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        String credentialsToml = """
            [registries.gitea]
            token = "Bearer %s"
            """.formatted(giteaConfig.getToken());
        Files.writeString(cargoDir.resolve("credentials.toml"), credentialsToml, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Path findCargoProjectDir(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root, 2)) {
            return paths
                .filter(p -> p.getFileName().toString().equals("Cargo.toml"))
                .findFirst()
                .orElseThrow(() -> new IOException("Cargo.toml not found"))
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
    @Override
    protected void runUpload(Path tempDir) throws IOException, InterruptedException {
        Path packageDir = findCargoProjectDir(tempDir);
        Path cargoTomlOrig = packageDir.resolve("Cargo.toml.orig");
        Files.deleteIfExists(cargoTomlOrig);
        Path cargoToml = packageDir.resolve("Cargo.toml");
        writeCargoConfig();
        List<String> lines = Files.readAllLines(cargoToml);
        List<String> newLines = new ArrayList<>();
        boolean insidePackage = false;
        boolean publishSet = false;

        for (String line : lines) {
            String trimmed = line.trim();
            
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                if (insidePackage && !publishSet) {
                    newLines.add("publish = [\"gitea\"]");
                    publishSet = true;
                }
                insidePackage = trimmed.equals("[package]");
            }

            newLines.add(line);
        }

        if (insidePackage && !publishSet) {
            newLines.add("publish = [\"gitea\"]");
        }

        Files.write(cargoToml, newLines, StandardOpenOption.TRUNCATE_EXISTING);

        log.info("config.toml:\n{}", Files.readString(Paths.get("/root/.cargo/config.toml")));
        log.info("credentials.toml:\n{}", Files.readString(Paths.get("/root/.cargo/credentials.toml")));
        log.info("Cargo output:\n{}", runCommand(packageDir, List.of("cargo", "publish", "--no-verify", "--allow-dirty","--registry", "gitea", "--verbose")));

    
    }


    
}

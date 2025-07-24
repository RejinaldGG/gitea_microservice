// package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.OutputStream;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Stream;
// import java.util.zip.GZIPInputStream;

// import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
// import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
// import org.apache.commons.compress.utils.IOUtils;
// import org.springframework.stereotype.Component;
// import org.springframework.web.multipart.MultipartFile;

// import com.example.gitea_microservice.domain.models.PackageManager;
// import com.example.gitea_microservice.domain.models.PublishType;
// import com.example.gitea_microservice.infrastructure.ports.GitClientPort;
// import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
// import com.example.gitea_microservice.infrastructure.ports.UploadPackagePort;
// import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// @Component
// @Slf4j
// @RequiredArgsConstructor
// public class CargoGiteaPackageUploader implements UploadPackagePort {
//     private final VersionExtractionUseCase versionExtractor;
//     private final GitClientPort giteaClient;
//     private final PackageValidatorUseCase packageValidator;
//     @Override
//     public boolean supports(PackageManager manager) {
//         return manager.getPublishType() == PublishType.MANAGER && manager == PackageManager.CARGO;
//     }

//     @Override
//     public void uploadPackage(PackageManager manager, MultipartFile file) {
//     packageValidator.validate(manager, file);

//     try {
//         Path tempDir = Files.createTempDirectory("cargo-crate-");
//         Path crateFile = tempDir.resolve(file.getOriginalFilename());
//         file.transferTo(crateFile.toFile());

//         unpackCrate(crateFile, tempDir);

//         Path cargoProjectDir = findCargoProjectDir(tempDir);
//         runCargoPublish(cargoProjectDir);

//         Files.deleteIfExists(crateFile);
//         // cleanup(tempDir); // рекомендую рекурсивную очистку
//     } catch (IOException | InterruptedException e) {
//         throw new RuntimeException("Failed to publish Cargo package", e);
//     }
// }

// private Path findCargoProjectDir(Path root) throws IOException {
//     try (Stream<Path> paths = Files.walk(root, 2)) {
//         return paths
//             .filter(p -> p.getFileName().toString().equals("Cargo.toml"))
//             .findFirst()
//             .orElseThrow(() -> new IOException("Cargo.toml not found"))
//             .getParent();
//     }
// }

// private void unpackCrate(Path crateFile, Path outputDir) throws IOException {
//     try (InputStream fis = Files.newInputStream(crateFile);
//          GZIPInputStream gzipIn = new GZIPInputStream(fis);
//          TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

//         TarArchiveEntry entry;
//         while ((entry = tarIn.getNextEntry()) != null) {
//             Path outPath = outputDir.resolve(entry.getName()).normalize();
//             if (!outPath.startsWith(outputDir)) {
//                 throw new IOException("Unsafe path: " + entry.getName());
//             }

//             if (entry.isDirectory()) {
//                 Files.createDirectories(outPath);
//             } else {
//                 Files.createDirectories(outPath.getParent());
//                 try (OutputStream out = Files.newOutputStream(outPath)) {
//                     IOUtils.copy(tarIn, out);
//                 }
//             }
//         }
//     }
// }
// private void runCargoPublish(Path packageDir) throws IOException, InterruptedException {
//     ProcessBuilder pb = new ProcessBuilder()
//         .command("cargo", "publish", "--no-verify", "--allow-dirty")
//         .directory(packageDir.toFile())
//         .redirectErrorStream(true);
//     Path cargoToml = packageDir.resolve("Cargo.toml");
// List<String> lines = Files.readAllLines(cargoToml);
// boolean hasPublish = lines.stream().anyMatch(line -> line.trim().startsWith("publish"));
// if (!hasPublish) {
//     List<String> newLines = new ArrayList<>();
//     for (String line : lines) {
//         newLines.add(line);
//         if (line.trim().startsWith("[package]")) {
//             newLines.add("publish = [\"gitea\"]"); 
//         }
//     }
//     Files.write(cargoToml, newLines);
// }

//     Map<String, String> env = pb.environment();
//     env.put("CARGO_REGISTRIES_GITEA_INDEX", "sparse+http://gitea:3000/api/packages/gitea/cargo/");
//     env.put("CARGO_REGISTRIES_GITEA_TOKEN", "Bearer 86ff883b16662b8594e5105ddaf5015b6c70fb6a");
//     env.put("CARGO_REGISTRIES_DEFAULT", "gitea");

//     Process process = pb.start();

//     StringBuilder output = new StringBuilder();
//     try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//         String line;
//         while ((line = reader.readLine()) != null) {
//             output.append(line).append("\n");
//         }
//     }

//     int exitCode = process.waitFor();
//     log.info("cargo publish output:\n{}", output);

//     if (exitCode != 0) {
//         throw new RuntimeException("cargo publish failed with exit code " + exitCode);
//     }
// }

    
// }

// package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Stream;

// import org.springframework.stereotype.Component;
// import org.yaml.snakeyaml.Yaml;

// import com.example.gitea_microservice.domain.models.PackageManager;
// import com.example.gitea_microservice.infrastructure.config.GiteaConfig;
// import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
// import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

// import lombok.extern.slf4j.Slf4j;

// @Component
// @Slf4j
// public class PubPackageUploader extends AbstractManagerPackageUploader {
//     private final GiteaConfig giteaConfig;
//     public PubPackageUploader(
//         PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig,VersionExtractionUseCase versionExtractor) {
//             super(packageValidator);
//             this.giteaConfig = giteaConfig;

//     }
//     @Override
//     public boolean supports(PackageManager manager) {
//         return manager == PackageManager.PUB;
//     }
//     protected String runCommandWithEnv(Path packageDir, List<String> command, Map<String, String> additionalEnv) {
//         ProcessBuilder pb = new ProcessBuilder()
//                 .command(command)
//                 .directory(packageDir.toFile())
//                 .redirectErrorStream(true);

//         if (additionalEnv != null) {
//             Map<String, String> env = pb.environment();
//             env.putAll(additionalEnv);
//         }

//         StringBuilder output = new StringBuilder();
//         try {
//             Process process = pb.start();

//             try (BufferedReader reader = new BufferedReader(
//                     new InputStreamReader(process.getInputStream()))) {
//                 String line;
//                 while ((line = reader.readLine()) != null) {
//                     output.append(line).append("\n");
//                 }
//             }
//             int exitCode = process.waitFor();
//             if (exitCode != 0) {
//                 throw new RuntimeException("Command failed with exit code " + exitCode + "\nOutput:\n" + output);
//             }
//                 return output.toString();

//             } catch (IOException | InterruptedException e) {
//                 throw new RuntimeException("Failed to run command: " + e.getMessage(), e);
//             }
//     }
//     private Path findProjectDir(Path root) throws IOException {
//         try (Stream<Path> paths = Files.walk(root, 2)) {
//             return paths
//                 .filter(p -> p.getFileName().toString().equals("pubspec.yaml"))
//                 .findFirst()
//                 .orElseThrow(() -> new IOException("pubspec.yaml not found"))
//                 .getParent();
//         }
//     }
    
//     private void addPublishTo(Path pubspecPath, String registryUrl) throws IOException {
    
//         Yaml yaml = new Yaml();
//         Map<String, Object> yamlMap = yaml.load(Files.newBufferedReader(pubspecPath));
        
    
//         yamlMap.put("publish_to", registryUrl);
        
    
//         String updatedYaml = yaml.dump(yamlMap);
//         Files.write(pubspecPath, updatedYaml.getBytes());
        
//     }

//     @Override
//     protected void runUpload(Path tempDir) throws IOException, InterruptedException {
//         Path pubDir = findProjectDir(tempDir);
//         Path metadata = pubDir.resolve("pubspec.yaml");
//         String registryUrl = "%s/api/packages/%s/pub".formatted(giteaConfig.getUrl(), giteaConfig.getOwner());
//         addPublishTo(metadata, registryUrl);
//         Map<String, String> env = new HashMap<>();
//         env.put("PUB_TOKEN", giteaConfig.getToken());
//         log.info("Pub output:\n{}", runCommandWithEnv(pubDir, List.of("dart", "pub", "token", "add", "--env-var=PUB_TOKEN", registryUrl),env));
//         log.info("Pub output:\n{}", runCommandWithEnv(pubDir, List.of("dart", "pub", "publish", "--skip-validation"),env));
//     }
// }
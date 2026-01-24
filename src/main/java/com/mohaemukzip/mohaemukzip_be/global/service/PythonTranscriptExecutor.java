package com.mohaemukzip.mohaemukzip_be.global.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PythonTranscriptExecutor {

    @Value("${transcript.script-path}")
    private String scriptPath;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public String fetchTranscriptJson(String videoId) {
        if (!"prod".equals(activeProfile)) {
            return """
            [
              {"text":"재료를 준비합니다","start":0,"duration":5},
              {"text":"고기를 볶습니다","start":30,"duration":10},
              {"text":"양념을 넣고 끓입니다","start":90,"duration":20}
            ]
        """;
        }

        return runPython(videoId);
    }

    private String runPython(String videoId) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python3",
                    scriptPath,
                    videoId
            );

            pb.directory(new File("/app"));
            pb.redirectErrorStream(true);

            Process process = pb.start();

            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
                try (InputStream is = process.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                outputFuture.cancel(true);
                throw new RuntimeException("Transcript script timeout");
            }

            String output = outputFuture.get(1, TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                throw new RuntimeException("Transcript script failed: " + output);
            }

            return output;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch youtube transcript", e);
        }
    }
}
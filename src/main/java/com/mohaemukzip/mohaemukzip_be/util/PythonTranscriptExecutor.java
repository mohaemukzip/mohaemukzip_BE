package com.mohaemukzip.mohaemukzip_be.util;

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

    @Value("${transcript.script-path:/app/scripts/get_youtube_transcript.py}")
    private String scriptPath;

    public String fetchTranscriptJson(String videoId) {
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

            long startNs = System.nanoTime();
            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                outputFuture.cancel(true);
                throw new RuntimeException("Transcript script timeout");
            }

            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            long remainingMs = Math.max(1000, 15_000 - elapsedMs);
            String output = outputFuture.get(remainingMs, TimeUnit.MILLISECONDS);

            if (process.exitValue() != 0) {
                throw new RuntimeException("Transcript script failed: " + output);
            }

            return output;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch youtube transcript", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch youtube transcript", e);
        }
    }
}
package com.mohaemukzip.mohaemukzip_be.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
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

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Transcript script timeout");
            }

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            if (process.exitValue() != 0) {
                throw new RuntimeException("Transcript script failed: " + output);
            }

            return output;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch youtube transcript", e);
        }
    }
}
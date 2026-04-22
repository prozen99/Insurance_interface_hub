package com.insurancehub.protocol.batch.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class BatchDemoStorage {

    private final BatchProperties properties;

    public BatchDemoStorage(BatchProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() throws IOException {
        Files.createDirectories(outputDirectory());
    }

    public Path outputDirectory() {
        return Path.of(properties.getDemo().getOutputDirectory()).toAbsolutePath().normalize();
    }
}

package com.insurancehub.protocol.filetransfer.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class FileTransferDemoStorage {

    private static final String SAMPLE_UPLOAD = """
            policyNo=POL-001
            eventType=POLICY_DOCUMENT_UPLOAD
            message=Sample file prepared for Phase 6 upload demos.
            """;

    private static final String SAMPLE_DOWNLOAD = """
            policyNo=POL-001
            documentType=TRANSFER_RESULT
            message=Sample file prepared by the local file-transfer demo server.
            """;

    private final FileTransferProperties properties;

    public FileTransferDemoStorage(FileTransferProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() throws IOException {
        Files.createDirectories(localInputDirectory());
        Files.createDirectories(localDownloadDirectory());
        Files.createDirectories(sftpRemoteRoot().resolve("inbox"));
        Files.createDirectories(sftpRemoteRoot().resolve("outbox"));
        Files.createDirectories(ftpRemoteRoot().resolve("inbox"));
        Files.createDirectories(ftpRemoteRoot().resolve("outbox"));
        writeIfMissing(localInputDirectory().resolve("sample-upload.txt"), SAMPLE_UPLOAD);
        writeIfMissing(sftpRemoteRoot().resolve("outbox").resolve("sample-download.txt"), SAMPLE_DOWNLOAD);
        writeIfMissing(ftpRemoteRoot().resolve("outbox").resolve("sample-download.txt"), SAMPLE_DOWNLOAD);
    }

    public Path rootDirectory() {
        return Path.of(properties.getDemo().getRootDirectory()).toAbsolutePath().normalize();
    }

    public Path localBaseDirectory() {
        return rootDirectory().resolve("local");
    }

    public Path localInputDirectory() {
        return localBaseDirectory().resolve("input");
    }

    public Path localDownloadDirectory() {
        return localBaseDirectory().resolve("download");
    }

    public Path sftpRemoteRoot() {
        return rootDirectory().resolve("remote").resolve("sftp");
    }

    public Path ftpRemoteRoot() {
        return rootDirectory().resolve("remote").resolve("ftp");
    }

    private void writeIfMissing(Path path, String content) throws IOException {
        if (Files.notExists(path)) {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        }
    }
}

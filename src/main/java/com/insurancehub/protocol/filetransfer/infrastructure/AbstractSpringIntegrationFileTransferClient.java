package com.insurancehub.protocol.filetransfer.infrastructure;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.insurancehub.protocol.filetransfer.application.FileTransferClient;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.util.StringUtils;

public abstract class AbstractSpringIntegrationFileTransferClient implements FileTransferClient {

    @Override
    public void upload(FileTransferConfig config, Path localFile, String remoteFilePath) {
        try {
            RemoteFileTemplate<?> template = remoteFileTemplate(config);
            template.execute(session -> {
                ensureRemoteDirectory(session, parentDirectory(remoteFilePath));
                try (InputStream inputStream = Files.newInputStream(localFile)) {
                    session.write(inputStream, remoteFilePath);
                }
                return null;
            });
        } catch (Exception exception) {
            throw new IllegalStateException("File upload failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void download(FileTransferConfig config, String remoteFilePath, Path localFile) {
        try {
            Files.createDirectories(localFile.getParent());
            RemoteFileTemplate<?> template = remoteFileTemplate(config);
            template.execute(session -> {
                try (OutputStream outputStream = Files.newOutputStream(localFile)) {
                    session.read(remoteFilePath, outputStream);
                }
                return null;
            });
        } catch (Exception exception) {
            throw new IllegalStateException("File download failed: " + exception.getMessage(), exception);
        }
    }

    protected abstract RemoteFileTemplate<?> remoteFileTemplate(FileTransferConfig config);

    private void ensureRemoteDirectory(Session<?> session, String remoteDirectory) {
        if (!StringUtils.hasText(remoteDirectory) || "/".equals(remoteDirectory)) {
            return;
        }

        try {
            String normalized = remoteDirectory.replace('\\', '/');
            StringBuilder current = new StringBuilder();
            for (String part : normalized.split("/")) {
                if (!StringUtils.hasText(part)) {
                    continue;
                }
                current.append('/').append(part);
                String directory = current.toString();
                if (!session.exists(directory)) {
                    session.mkdir(directory);
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not prepare remote directory: " + remoteDirectory, exception);
        }
    }

    private String parentDirectory(String remoteFilePath) {
        int index = remoteFilePath.lastIndexOf('/');
        if (index <= 0) {
            return "/";
        }
        return remoteFilePath.substring(0, index);
    }
}

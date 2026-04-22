package com.insurancehub.protocol.sftp;

import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.filetransfer.application.FileTransferSecretResolver;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import com.insurancehub.protocol.filetransfer.infrastructure.AbstractSpringIntegrationFileTransferClient;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Component;

@Component
public class SftpFileTransferClient extends AbstractSpringIntegrationFileTransferClient {

    private final FileTransferSecretResolver secretResolver;

    public SftpFileTransferClient(FileTransferSecretResolver secretResolver) {
        this.secretResolver = secretResolver;
    }

    @Override
    public ProtocolType supports() {
        return ProtocolType.SFTP;
    }

    @Override
    protected RemoteFileTemplate<?> remoteFileTemplate(FileTransferConfig config) {
        DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory(true);
        sessionFactory.setHost(config.getHost());
        sessionFactory.setPort(config.getPort());
        sessionFactory.setUser(config.getUsername());
        sessionFactory.setPassword(secretResolver.resolve(config.getSecretReference()));
        sessionFactory.setAllowUnknownKeys(true);
        sessionFactory.setTimeout(config.getTimeoutMillis());
        return new SftpRemoteFileTemplate(sessionFactory);
    }
}

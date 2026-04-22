package com.insurancehub.protocol.ftp;

import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.filetransfer.application.FileTransferSecretResolver;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import com.insurancehub.protocol.filetransfer.infrastructure.AbstractSpringIntegrationFileTransferClient;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.stereotype.Component;

@Component
public class FtpFileTransferClient extends AbstractSpringIntegrationFileTransferClient {

    private final FileTransferSecretResolver secretResolver;

    public FtpFileTransferClient(FileTransferSecretResolver secretResolver) {
        this.secretResolver = secretResolver;
    }

    @Override
    public ProtocolType supports() {
        return ProtocolType.FTP;
    }

    @Override
    protected RemoteFileTemplate<?> remoteFileTemplate(FileTransferConfig config) {
        DefaultFtpSessionFactory sessionFactory = new DefaultFtpSessionFactory();
        sessionFactory.setHost(config.getHost());
        sessionFactory.setPort(config.getPort());
        sessionFactory.setUsername(config.getUsername());
        sessionFactory.setPassword(secretResolver.resolve(config.getSecretReference()));
        sessionFactory.setClientMode(config.isPassiveMode()
                ? FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE
                : FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE);
        sessionFactory.setConnectTimeout(config.getTimeoutMillis());
        sessionFactory.setDefaultTimeout(config.getTimeoutMillis());
        sessionFactory.setDataTimeout(config.getTimeoutMillis());
        return new FtpRemoteFileTemplate(sessionFactory);
    }
}

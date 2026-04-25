package com.insurancehub.protocol.filetransfer.config;

import java.nio.file.Files;
import java.util.List;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FileTransferProperties.class)
public class LocalFileTransferServerConfig {

    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "app.file-transfer.sftp", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SshServer localSftpServer(
            FileTransferProperties properties,
            FileTransferDemoStorage storage
    ) throws Exception {
        Files.createDirectories(storage.sftpRemoteRoot());

        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setHost(properties.getDemo().getHost());
        sshServer.setPort(properties.getSftp().getPort());
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
                storage.rootDirectory().resolve("sftp-host-key.ser")
        ));
        sshServer.setPasswordAuthenticator((username, password, session) ->
                properties.getDemo().getUsername().equals(username)
                        && properties.getDemo().getPassword().equals(password)
        );
        sshServer.setFileSystemFactory(new VirtualFileSystemFactory(storage.sftpRemoteRoot()));
        sshServer.setSubsystemFactories(List.of(new SftpSubsystemFactory.Builder().build()));
        sshServer.start();
        return sshServer;
    }

    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "app.file-transfer.ftp", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FtpServer localFtpServer(
            FileTransferProperties properties,
            FileTransferDemoStorage storage
    ) throws Exception {
        Files.createDirectories(storage.ftpRemoteRoot());

        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setServerAddress(properties.getDemo().getHost());
        listenerFactory.setPort(properties.getFtp().getPort());
        serverFactory.addListener("default", listenerFactory.createListener());

        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(false);
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

        BaseUser user = new BaseUser();
        user.setName(properties.getDemo().getUsername());
        user.setPassword(properties.getDemo().getPassword());
        user.setHomeDirectory(storage.ftpRemoteRoot().toString());
        user.setAuthorities(List.<Authority>of(new WritePermission()));
        serverFactory.getUserManager().save(user);

        FtpServer ftpServer = serverFactory.createServer();
        ftpServer.start();
        return ftpServer;
    }
}

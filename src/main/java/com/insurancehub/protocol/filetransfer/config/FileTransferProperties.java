package com.insurancehub.protocol.filetransfer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.file-transfer")
public class FileTransferProperties {

    private final Demo demo = new Demo();
    private final Sftp sftp = new Sftp();
    private final Ftp ftp = new Ftp();

    public Demo getDemo() {
        return demo;
    }

    public Sftp getSftp() {
        return sftp;
    }

    public Ftp getFtp() {
        return ftp;
    }

    public static class Demo {

        private String rootDirectory = "build/file-transfer-demo";

        private String host = "127.0.0.1";

        private String username = "demo";

        private String password = "local-demo-password";

        public String getRootDirectory() {
            return rootDirectory;
        }

        public void setRootDirectory(String rootDirectory) {
            this.rootDirectory = rootDirectory;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Sftp {

        private boolean enabled = true;

        private int port = 10022;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Ftp {

        private boolean enabled = true;

        private int port = 10021;

        private boolean passiveMode = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isPassiveMode() {
            return passiveMode;
        }

        public void setPassiveMode(boolean passiveMode) {
            this.passiveMode = passiveMode;
        }
    }
}

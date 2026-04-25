package com.insurancehub.protocol.filetransfer.application;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.stereotype.Component;

@Component
public class FileTransferClientFactory {

    private final Map<ProtocolType, FileTransferClient> clients;

    public FileTransferClientFactory(List<FileTransferClient> clients) {
        this.clients = new EnumMap<>(ProtocolType.class);
        for (FileTransferClient client : clients) {
            this.clients.put(client.supports(), client);
        }
    }

    public FileTransferClient getClient(ProtocolType protocolType) {
        FileTransferClient client = clients.get(protocolType);
        if (client == null) {
            throw new IllegalStateException("No file transfer client registered for protocol: " + protocolType);
        }
        return client;
    }
}

package com.insurancehub.protocol.filetransfer.application;

import java.util.Optional;

import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferConfigRepository;
import com.insurancehub.protocol.filetransfer.presentation.form.FileTransferConfigForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileTransferConfigService {

    public static final String SAMPLE_UPLOAD_FILE_NAME = "sample-upload.txt";
    public static final String SAMPLE_DOWNLOAD_FILE_NAME = "sample-download.txt";

    private final FileTransferConfigRepository fileTransferConfigRepository;
    private final InterfaceDefinitionRepository interfaceDefinitionRepository;

    public FileTransferConfigService(
            FileTransferConfigRepository fileTransferConfigRepository,
            InterfaceDefinitionRepository interfaceDefinitionRepository
    ) {
        this.fileTransferConfigRepository = fileTransferConfigRepository;
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<FileTransferConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId) {
        return fileTransferConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId);
    }

    @Transactional(readOnly = true)
    public FileTransferConfigForm formForInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = getFileTransferInterface(interfaceDefinitionId);
        return fileTransferConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .map(FileTransferConfigForm::from)
                .orElseGet(() -> FileTransferConfigForm.empty(interfaceDefinition.getProtocolType()));
    }

    @Transactional(readOnly = true)
    public FileTransferConfig getActiveForExecution(InterfaceDefinition interfaceDefinition) {
        assertFileTransferInterface(interfaceDefinition);
        return fileTransferConfigRepository.findByInterfaceDefinitionIdAndProtocolType(
                        interfaceDefinition.getId(),
                        interfaceDefinition.getProtocolType()
                )
                .filter(FileTransferConfig::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Active file transfer configuration is required before execution."
                ));
    }

    @Transactional
    public FileTransferConfig save(Long interfaceDefinitionId, FileTransferConfigForm form) {
        InterfaceDefinition interfaceDefinition = getFileTransferInterface(interfaceDefinitionId);
        ProtocolType protocolType = interfaceDefinition.getProtocolType();
        if (form.getProtocolType() != protocolType) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File transfer protocol does not match interface protocol.");
        }

        FileTransferConfig config = fileTransferConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId)
                .orElseGet(() -> FileTransferConfig.create(
                        interfaceDefinition,
                        protocolType,
                        form.getHost(),
                        form.getPort(),
                        form.getUsername(),
                        form.getSecretReference(),
                        form.getBaseRemotePath(),
                        form.getLocalPath(),
                        form.getFileNamePattern(),
                        form.isPassiveMode(),
                        form.getTimeoutMillis(),
                        form.isActive()
                ));

        config.update(
                protocolType,
                form.getHost(),
                form.getPort(),
                form.getUsername(),
                form.getSecretReference(),
                form.getBaseRemotePath(),
                form.getLocalPath(),
                form.getFileNamePattern(),
                form.isPassiveMode(),
                form.getTimeoutMillis(),
                form.isActive()
        );
        return fileTransferConfigRepository.save(config);
    }

    private InterfaceDefinition getFileTransferInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionRepository.findDetailById(interfaceDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface definition not found"));
        assertFileTransferInterface(interfaceDefinition);
        return interfaceDefinition;
    }

    private void assertFileTransferInterface(InterfaceDefinition interfaceDefinition) {
        if (interfaceDefinition.getProtocolType() != ProtocolType.SFTP && interfaceDefinition.getProtocolType() != ProtocolType.FTP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File transfer configuration is only available for SFTP or FTP interfaces.");
        }
    }
}

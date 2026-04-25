package com.insurancehub.protocol.filetransfer.infrastructure.repository;

import java.util.Optional;

import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileTransferConfigRepository extends JpaRepository<FileTransferConfig, Long> {

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    Optional<FileTransferConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId);

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    Optional<FileTransferConfig> findByInterfaceDefinitionIdAndProtocolType(Long interfaceDefinitionId, ProtocolType protocolType);
}

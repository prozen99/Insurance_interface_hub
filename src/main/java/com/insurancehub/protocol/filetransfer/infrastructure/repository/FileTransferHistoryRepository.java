package com.insurancehub.protocol.filetransfer.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.insurancehub.protocol.filetransfer.domain.FileTransferStatus;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileTransferHistoryRepository extends JpaRepository<FileTransferHistory, Long> {

    long countByTransferStatusAndCreatedAtBetween(
            FileTransferStatus transferStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "fileTransferConfig"})
    List<FileTransferHistory> findByExecutionIdOrderByCreatedAtDesc(Long executionId);

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "fileTransferConfig"})
    List<FileTransferHistory> findTop30ByOrderByCreatedAtDesc();
}

package com.insurancehub.protocol.filetransfer.application;

import java.util.List;

import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferHistory;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileTransferHistoryService {

    private final FileTransferHistoryRepository fileTransferHistoryRepository;

    public FileTransferHistoryService(FileTransferHistoryRepository fileTransferHistoryRepository) {
        this.fileTransferHistoryRepository = fileTransferHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<FileTransferHistory> findByExecutionId(Long executionId) {
        return fileTransferHistoryRepository.findByExecutionIdOrderByCreatedAtDesc(executionId);
    }

    @Transactional(readOnly = true)
    public List<FileTransferHistory> recentTransfers() {
        return fileTransferHistoryRepository.findTop30ByOrderByCreatedAtDesc();
    }
}

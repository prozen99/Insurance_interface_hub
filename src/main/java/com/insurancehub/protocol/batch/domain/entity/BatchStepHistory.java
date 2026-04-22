package com.insurancehub.protocol.batch.domain.entity;

import java.time.LocalDateTime;

import com.insurancehub.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "batch_step_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchStepHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_run_history_id", nullable = false)
    private BatchRunHistory batchRunHistory;

    @Column(name = "step_name", nullable = false, length = 160)
    private String stepName;

    @Column(name = "step_status", nullable = false, length = 40)
    private String stepStatus;

    @Column(name = "read_count")
    private Long readCount;

    @Column(name = "write_count")
    private Long writeCount;

    @Column(name = "commit_count")
    private Long commitCount;

    @Column(name = "rollback_count")
    private Long rollbackCount;

    @Column(name = "skip_count")
    private Long skipCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "exit_code", length = 80)
    private String exitCode;

    @Column(name = "exit_description", length = 2500)
    private String exitDescription;

    private BatchStepHistory(
            BatchRunHistory batchRunHistory,
            String stepName,
            String stepStatus,
            long readCount,
            long writeCount,
            long commitCount,
            long rollbackCount,
            long skipCount,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String exitCode,
            String exitDescription
    ) {
        this.batchRunHistory = batchRunHistory;
        this.stepName = stepName;
        this.stepStatus = stepStatus;
        this.readCount = readCount;
        this.writeCount = writeCount;
        this.commitCount = commitCount;
        this.rollbackCount = rollbackCount;
        this.skipCount = skipCount;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.exitCode = exitCode;
        this.exitDescription = trim(exitDescription, 2500);
    }

    public static BatchStepHistory create(
            BatchRunHistory batchRunHistory,
            String stepName,
            String stepStatus,
            long readCount,
            long writeCount,
            long commitCount,
            long rollbackCount,
            long skipCount,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String exitCode,
            String exitDescription
    ) {
        return new BatchStepHistory(
                batchRunHistory,
                stepName,
                stepStatus,
                readCount,
                writeCount,
                commitCount,
                rollbackCount,
                skipCount,
                startedAt,
                finishedAt,
                exitCode,
                exitDescription
        );
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

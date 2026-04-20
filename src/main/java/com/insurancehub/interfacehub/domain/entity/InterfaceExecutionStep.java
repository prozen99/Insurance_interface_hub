package com.insurancehub.interfacehub.domain.entity;

import java.time.LocalDateTime;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.ExecutionStepStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "interface_execution_step")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterfaceExecutionStep extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_execution_id", nullable = false)
    private InterfaceExecution execution;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "step_name", nullable = false, length = 120)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ExecutionStepStatus stepStatus;

    @Column(length = 2000)
    private String message;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    private InterfaceExecutionStep(
            InterfaceExecution execution,
            int stepOrder,
            String stepName,
            ExecutionStepStatus stepStatus,
            String message,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {
        this.execution = execution;
        this.stepOrder = stepOrder;
        this.stepName = stepName;
        this.stepStatus = stepStatus;
        this.message = message;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static InterfaceExecutionStep create(
            InterfaceExecution execution,
            int stepOrder,
            String stepName,
            ExecutionStepStatus stepStatus,
            String message,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {
        return new InterfaceExecutionStep(
                execution,
                stepOrder,
                stepName,
                stepStatus,
                message,
                startedAt,
                finishedAt
        );
    }
}

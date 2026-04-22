package com.insurancehub.interfacehub.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterfaceExecutionRepository extends JpaRepository<InterfaceExecution, Long> {

    boolean existsByExecutionNo(String executionNo);

    long countByExecutionStatusAndStartedAtBetween(
            ExecutionStatus executionStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    long countByStartedAtBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);

    long countByProtocolTypeAndStartedAtBetween(
            ProtocolType protocolType,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    long countByProtocolTypeAndExecutionStatusAndStartedAtBetween(
            ProtocolType protocolType,
            ExecutionStatus executionStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    List<InterfaceExecution> findTop10ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    List<InterfaceExecution> findTop20ByExecutionStatusOrderByCreatedAtDesc(ExecutionStatus executionStatus);

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    List<InterfaceExecution> findTop5ByInterfaceDefinitionIdOrderByCreatedAtDesc(Long interfaceDefinitionId);

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    @Query("""
            select e
            from InterfaceExecution e
            join e.interfaceDefinition d
            where e.startedAt >= :startInclusive
              and e.startedAt < :endExclusive
            order by e.startedAt asc
            """)
    List<InterfaceExecution> findTrendSource(
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    @Query("""
            select d.id, d.interfaceCode, d.interfaceName, d.protocolType, count(e)
            from InterfaceExecution e
            join e.interfaceDefinition d
            where e.executionStatus = :executionStatus
              and e.startedAt >= :startInclusive
              and e.startedAt < :endExclusive
            group by d.id, d.interfaceCode, d.interfaceName, d.protocolType
            order by count(e) desc, d.interfaceCode asc
            """)
    List<Object[]> findTopFailedInterfaces(
            @Param("executionStatus") ExecutionStatus executionStatus,
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive,
            Pageable pageable
    );

    @Query("""
            select e
            from InterfaceExecution e
            join fetch e.interfaceDefinition d
            left join fetch e.retrySourceExecution source
            where e.id = :id
            """)
    Optional<InterfaceExecution> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    @Query("""
            select e
            from InterfaceExecution e
            join e.interfaceDefinition d
            where (:keyword is null
                or lower(e.executionNo) like lower(concat('%', :keyword, '%'))
                or lower(d.interfaceCode) like lower(concat('%', :keyword, '%'))
                or lower(d.interfaceName) like lower(concat('%', :keyword, '%')))
              and (:protocolType is null or e.protocolType = :protocolType)
              and (:executionStatus is null or e.executionStatus = :executionStatus)
              and (:triggerType is null or e.triggerType = :triggerType)
              and (:startedFrom is null or e.startedAt >= :startedFrom)
              and (:startedTo is null or e.startedAt < :startedTo)
            order by e.createdAt desc
            """)
    List<InterfaceExecution> search(
            @Param("keyword") String keyword,
            @Param("protocolType") ProtocolType protocolType,
            @Param("executionStatus") ExecutionStatus executionStatus,
            @Param("triggerType") ExecutionTriggerType triggerType,
            @Param("startedFrom") LocalDateTime startedFrom,
            @Param("startedTo") LocalDateTime startedTo
    );
}

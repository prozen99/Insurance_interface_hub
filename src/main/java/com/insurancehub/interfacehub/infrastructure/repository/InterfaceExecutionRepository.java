package com.insurancehub.interfacehub.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterfaceExecutionRepository extends JpaRepository<InterfaceExecution, Long> {

    boolean existsByExecutionNo(String executionNo);

    long countByExecutionStatusAndStartedAtBetween(
            ExecutionStatus executionStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    List<InterfaceExecution> findTop5ByInterfaceDefinitionIdOrderByCreatedAtDesc(Long interfaceDefinitionId);

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
            order by e.createdAt desc
            """)
    List<InterfaceExecution> search(
            @Param("keyword") String keyword,
            @Param("protocolType") ProtocolType protocolType,
            @Param("executionStatus") ExecutionStatus executionStatus
    );
}

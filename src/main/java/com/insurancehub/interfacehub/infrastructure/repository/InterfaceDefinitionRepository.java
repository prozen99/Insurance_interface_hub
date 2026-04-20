package com.insurancehub.interfacehub.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterfaceDefinitionRepository extends JpaRepository<InterfaceDefinition, Long> {

    boolean existsByInterfaceCode(String interfaceCode);

    boolean existsByInterfaceCodeAndIdNot(String interfaceCode, Long id);

    long countByStatus(InterfaceStatus status);

    @EntityGraph(attributePaths = {"partnerCompany", "internalSystem"})
    @Query("""
            select i
            from InterfaceDefinition i
            where (:keyword is null
                or lower(i.interfaceCode) like lower(concat('%', :keyword, '%'))
                or lower(i.interfaceName) like lower(concat('%', :keyword, '%')))
              and (:protocolType is null or i.protocolType = :protocolType)
              and (:status is null or i.status = :status)
            order by i.interfaceCode asc
            """)
    List<InterfaceDefinition> search(
            @Param("keyword") String keyword,
            @Param("protocolType") ProtocolType protocolType,
            @Param("status") InterfaceStatus status
    );

    @Query("""
            select i
            from InterfaceDefinition i
            left join fetch i.partnerCompany
            left join fetch i.internalSystem
            where i.id = :id
            """)
    Optional<InterfaceDefinition> findDetailById(@Param("id") Long id);
}

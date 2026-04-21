package com.insurancehub.protocol.soap.infrastructure.repository;

import java.util.Optional;

import com.insurancehub.protocol.soap.domain.entity.SoapEndpointConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoapEndpointConfigRepository extends JpaRepository<SoapEndpointConfig, Long> {

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    Optional<SoapEndpointConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId);
}

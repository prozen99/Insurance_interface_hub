package com.insurancehub.protocol.rest.infrastructure.repository;

import java.util.Optional;

import com.insurancehub.protocol.rest.domain.entity.RestEndpointConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestEndpointConfigRepository extends JpaRepository<RestEndpointConfig, Long> {

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    Optional<RestEndpointConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId);
}

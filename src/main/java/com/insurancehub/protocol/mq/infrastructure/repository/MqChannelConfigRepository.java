package com.insurancehub.protocol.mq.infrastructure.repository;

import java.util.Optional;

import com.insurancehub.protocol.mq.domain.entity.MqChannelConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MqChannelConfigRepository extends JpaRepository<MqChannelConfig, Long> {

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    Optional<MqChannelConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId);
}

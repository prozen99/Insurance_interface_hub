alter table mq_channel_config
    add column destination_name varchar(180) null after queue_name,
    add column message_type varchar(60) not null default 'TEXT' after routing_key,
    add column correlation_key_expression varchar(300) null after message_type,
    add column timeout_millis int not null default 5000 after correlation_key_expression,
    add column active_yn tinyint(1) not null default 1 after timeout_millis;

update mq_channel_config
set destination_name = queue_name
where destination_name is null;

create table mq_message_history (
    id bigint not null auto_increment,
    interface_execution_id bigint not null,
    interface_definition_id bigint not null,
    mq_channel_config_id bigint not null,
    message_id varchar(160) null,
    correlation_key varchar(180) not null,
    destination_name varchar(180) not null,
    message_type varchar(60) not null,
    publish_status varchar(40) not null,
    consume_status varchar(40) not null,
    outbound_payload longtext null,
    consumed_payload longtext null,
    result_message varchar(1000) null,
    error_code varchar(80) null,
    error_message varchar(2000) null,
    published_at datetime(6) null,
    consumed_at datetime(6) null,
    latency_ms bigint null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    key idx_mq_message_history_execution (interface_execution_id),
    key idx_mq_message_history_definition (interface_definition_id),
    key idx_mq_message_history_config (mq_channel_config_id),
    key idx_mq_message_history_correlation (correlation_key),
    key idx_mq_message_history_destination (destination_name),
    key idx_mq_message_history_status (publish_status, consume_status),
    constraint fk_mq_message_history_execution foreign key (interface_execution_id) references interface_execution (id),
    constraint fk_mq_message_history_definition foreign key (interface_definition_id) references interface_definition (id),
    constraint fk_mq_message_history_config foreign key (mq_channel_config_id) references mq_channel_config (id)
);

create index idx_mq_channel_config_active
    on mq_channel_config (active_yn);

insert into interface_definition (
    interface_code,
    interface_name,
    description,
    protocol_type,
    direction_type,
    status,
    partner_company_id,
    internal_system_id,
    owner_team,
    enabled_yn
)
select
    'IF_MQ_POLICY_001',
    'Policy event outbound MQ interface',
    'Sample MQ interface for Phase 5 local publish and consume demos',
    'MQ',
    'OUTBOUND',
    'ACTIVE',
    p.id,
    s.id,
    'Insurance Platform Team',
    1
from partner_company p
join internal_system s on s.system_code = 'POLICY_CORE'
where p.partner_code = 'LIFEPLUS'
  and not exists (
      select 1 from interface_definition where interface_code = 'IF_MQ_POLICY_001'
  );

insert into mq_channel_config (
    interface_definition_id,
    broker_type,
    queue_name,
    destination_name,
    exchange_name,
    routing_key,
    connection_alias,
    message_type,
    correlation_key_expression,
    timeout_millis,
    active_yn
)
select
    d.id,
    'EMBEDDED_ARTEMIS',
    'insurancehub.demo.policy.events',
    'insurancehub.demo.policy.events',
    null,
    'policy.event',
    'local-in-vm-artemis',
    'TEXT',
    'MQ-{executionNo}',
    5000,
    1
from interface_definition d
where d.interface_code = 'IF_MQ_POLICY_001'
  and not exists (
      select 1
      from mq_channel_config c
      where c.interface_definition_id = d.id
  );

update mq_channel_config c
join interface_definition d on d.id = c.interface_definition_id
set c.broker_type = coalesce(c.broker_type, 'EMBEDDED_ARTEMIS'),
    c.queue_name = coalesce(c.queue_name, 'insurancehub.demo.policy.events'),
    c.destination_name = coalesce(c.destination_name, c.queue_name, 'insurancehub.demo.policy.events'),
    c.routing_key = coalesce(c.routing_key, 'policy.event'),
    c.connection_alias = coalesce(c.connection_alias, 'local-in-vm-artemis'),
    c.message_type = coalesce(c.message_type, 'TEXT'),
    c.correlation_key_expression = coalesce(c.correlation_key_expression, 'MQ-{executionNo}'),
    c.timeout_millis = coalesce(c.timeout_millis, 5000),
    c.active_yn = 1
where d.interface_code = 'IF_MQ_POLICY_001';

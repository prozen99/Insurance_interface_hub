alter table file_transfer_config
    add column host varchar(160) null after host_alias,
    add column username varchar(120) null after port,
    add column secret_reference varchar(240) null after username,
    add column base_remote_path varchar(500) null after secret_reference,
    add column timeout_millis int not null default 5000 after passive_mode_yn,
    add column active_yn tinyint(1) not null default 1 after timeout_millis;

update file_transfer_config
set host = coalesce(host, host_alias, '127.0.0.1'),
    username = coalesce(username, 'demo'),
    secret_reference = coalesce(secret_reference, 'LOCAL_DEMO_FILE_TRANSFER_PASSWORD'),
    base_remote_path = coalesce(base_remote_path, remote_path, '/inbox'),
    timeout_millis = coalesce(timeout_millis, 5000),
    active_yn = coalesce(active_yn, 1);

create table file_transfer_history (
    id bigint not null auto_increment,
    interface_execution_id bigint not null,
    interface_definition_id bigint not null,
    file_transfer_config_id bigint not null,
    protocol_type varchar(20) not null,
    transfer_direction varchar(30) not null,
    local_file_name varchar(240) not null,
    local_file_path varchar(1000) not null,
    remote_file_path varchar(1000) not null,
    file_size_bytes bigint null,
    transfer_status varchar(40) not null,
    latency_millis bigint null,
    error_message varchar(2000) null,
    checksum_sha256 varchar(80) null,
    content_summary varchar(500) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    key idx_file_transfer_history_execution (interface_execution_id),
    key idx_file_transfer_history_definition (interface_definition_id),
    key idx_file_transfer_history_config (file_transfer_config_id),
    key idx_file_transfer_history_protocol (protocol_type),
    key idx_file_transfer_history_status (transfer_status),
    constraint fk_file_transfer_history_execution foreign key (interface_execution_id) references interface_execution (id),
    constraint fk_file_transfer_history_definition foreign key (interface_definition_id) references interface_definition (id),
    constraint fk_file_transfer_history_config foreign key (file_transfer_config_id) references file_transfer_config (id)
);

create index idx_file_transfer_config_active
    on file_transfer_config (active_yn);

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
    'IF_SFTP_POLICY_001',
    'Policy document SFTP transfer interface',
    'Sample SFTP interface for Phase 6 local upload and download demos',
    'SFTP',
    'BIDIRECTIONAL',
    'ACTIVE',
    p.id,
    s.id,
    'Insurance Platform Team',
    1
from partner_company p
join internal_system s on s.system_code = 'POLICY_CORE'
where p.partner_code = 'LIFEPLUS'
  and not exists (
      select 1 from interface_definition where interface_code = 'IF_SFTP_POLICY_001'
  );

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
    'IF_FTP_POLICY_001',
    'Policy document FTP transfer interface',
    'Sample FTP interface for Phase 6 local upload and download demos',
    'FTP',
    'BIDIRECTIONAL',
    'ACTIVE',
    p.id,
    s.id,
    'Insurance Platform Team',
    1
from partner_company p
join internal_system s on s.system_code = 'POLICY_CORE'
where p.partner_code = 'LIFEPLUS'
  and not exists (
      select 1 from interface_definition where interface_code = 'IF_FTP_POLICY_001'
  );

insert into file_transfer_config (
    interface_definition_id,
    transfer_protocol,
    host_alias,
    host,
    port,
    username,
    secret_reference,
    remote_path,
    base_remote_path,
    local_path,
    file_name_pattern,
    passive_mode_yn,
    timeout_millis,
    active_yn
)
select
    d.id,
    'SFTP',
    'local-sftp-demo',
    '127.0.0.1',
    10022,
    'demo',
    'LOCAL_DEMO_FILE_TRANSFER_PASSWORD',
    '/inbox',
    '/inbox',
    'build/file-transfer-demo/local',
    '*.txt',
    0,
    5000,
    1
from interface_definition d
where d.interface_code = 'IF_SFTP_POLICY_001'
  and not exists (
      select 1
      from file_transfer_config c
      where c.interface_definition_id = d.id
  );

insert into file_transfer_config (
    interface_definition_id,
    transfer_protocol,
    host_alias,
    host,
    port,
    username,
    secret_reference,
    remote_path,
    base_remote_path,
    local_path,
    file_name_pattern,
    passive_mode_yn,
    timeout_millis,
    active_yn
)
select
    d.id,
    'FTP',
    'local-ftp-demo',
    '127.0.0.1',
    10021,
    'demo',
    'LOCAL_DEMO_FILE_TRANSFER_PASSWORD',
    '/inbox',
    '/inbox',
    'build/file-transfer-demo/local',
    '*.txt',
    1,
    5000,
    1
from interface_definition d
where d.interface_code = 'IF_FTP_POLICY_001'
  and not exists (
      select 1
      from file_transfer_config c
      where c.interface_definition_id = d.id
  );

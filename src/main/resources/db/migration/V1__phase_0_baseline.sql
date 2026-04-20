create table admin_user (
    id bigint not null auto_increment,
    login_id varchar(80) not null,
    display_name varchar(120) not null,
    email varchar(180) null,
    role_code varchar(40) not null,
    status varchar(30) not null,
    last_login_at datetime(6) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_admin_user_login_id (login_id),
    unique key uk_admin_user_email (email)
);

create table partner_company (
    id bigint not null auto_increment,
    partner_code varchar(60) not null,
    partner_name varchar(160) not null,
    business_type varchar(60) null,
    active_yn tinyint(1) not null default 1,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_partner_company_code (partner_code)
);

create table internal_system (
    id bigint not null auto_increment,
    system_code varchar(60) not null,
    system_name varchar(160) not null,
    owner_department varchar(120) null,
    active_yn tinyint(1) not null default 1,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_internal_system_code (system_code)
);

create table interface_definition (
    id bigint not null auto_increment,
    interface_code varchar(80) not null,
    interface_name varchar(180) not null,
    description varchar(1000) null,
    protocol_type varchar(30) not null,
    direction_type varchar(30) not null,
    partner_company_id bigint null,
    internal_system_id bigint null,
    owner_team varchar(120) null,
    enabled_yn tinyint(1) not null default 1,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_interface_definition_code (interface_code),
    key idx_interface_definition_protocol (protocol_type),
    key idx_interface_definition_partner (partner_company_id),
    key idx_interface_definition_system (internal_system_id),
    constraint fk_interface_definition_partner foreign key (partner_company_id) references partner_company (id),
    constraint fk_interface_definition_system foreign key (internal_system_id) references internal_system (id)
);

create table interface_execution (
    id bigint not null auto_increment,
    interface_definition_id bigint not null,
    execution_key varchar(120) not null,
    trigger_type varchar(40) not null,
    status varchar(40) not null,
    requested_by varchar(120) null,
    started_at datetime(6) null,
    finished_at datetime(6) null,
    duration_ms bigint null,
    error_code varchar(80) null,
    error_message varchar(2000) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_interface_execution_key (execution_key),
    key idx_interface_execution_definition (interface_definition_id),
    key idx_interface_execution_status (status),
    key idx_interface_execution_started_at (started_at),
    constraint fk_interface_execution_definition foreign key (interface_definition_id) references interface_definition (id)
);

create table interface_execution_step (
    id bigint not null auto_increment,
    interface_execution_id bigint not null,
    step_order int not null,
    step_name varchar(120) not null,
    status varchar(40) not null,
    started_at datetime(6) null,
    finished_at datetime(6) null,
    message varchar(2000) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    key idx_interface_execution_step_execution (interface_execution_id),
    constraint fk_interface_execution_step_execution foreign key (interface_execution_id) references interface_execution (id)
);

create table interface_retry_task (
    id bigint not null auto_increment,
    interface_execution_id bigint not null,
    interface_definition_id bigint not null,
    retry_status varchar(40) not null,
    retry_count int not null default 0,
    max_retry_count int not null default 3,
    next_retry_at datetime(6) null,
    last_error_message varchar(2000) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    key idx_interface_retry_task_status (retry_status),
    key idx_interface_retry_task_next_retry_at (next_retry_at),
    constraint fk_interface_retry_task_execution foreign key (interface_execution_id) references interface_execution (id),
    constraint fk_interface_retry_task_definition foreign key (interface_definition_id) references interface_definition (id)
);

create table audit_log (
    id bigint not null auto_increment,
    actor_id bigint null,
    action_type varchar(80) not null,
    target_type varchar(80) not null,
    target_id varchar(120) null,
    request_ip varchar(80) null,
    user_agent varchar(500) null,
    before_value json null,
    after_value json null,
    created_at datetime(6) not null default current_timestamp(6),
    primary key (id),
    key idx_audit_log_actor (actor_id),
    key idx_audit_log_target (target_type, target_id),
    key idx_audit_log_created_at (created_at),
    constraint fk_audit_log_actor foreign key (actor_id) references admin_user (id)
);

create table rest_endpoint_config (
    id bigint not null auto_increment,
    interface_definition_id bigint not null,
    http_method varchar(20) not null,
    endpoint_url varchar(500) not null,
    timeout_millis int not null default 5000,
    auth_type varchar(40) not null default 'NONE',
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_rest_endpoint_config_definition (interface_definition_id),
    constraint fk_rest_endpoint_config_definition foreign key (interface_definition_id) references interface_definition (id)
);

create table soap_endpoint_config (
    id bigint not null auto_increment,
    interface_definition_id bigint not null,
    service_url varchar(500) not null,
    soap_action varchar(300) null,
    namespace_uri varchar(300) null,
    timeout_millis int not null default 10000,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_soap_endpoint_config_definition (interface_definition_id),
    constraint fk_soap_endpoint_config_definition foreign key (interface_definition_id) references interface_definition (id)
);

create table mq_channel_config (
    id bigint not null auto_increment,
    interface_definition_id bigint not null,
    broker_type varchar(60) not null,
    queue_name varchar(180) not null,
    exchange_name varchar(180) null,
    routing_key varchar(180) null,
    connection_alias varchar(120) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_mq_channel_config_definition (interface_definition_id),
    constraint fk_mq_channel_config_definition foreign key (interface_definition_id) references interface_definition (id)
);

create table file_transfer_config (
    id bigint not null auto_increment,
    interface_definition_id bigint not null,
    transfer_protocol varchar(20) not null,
    host_alias varchar(120) not null,
    port int null,
    remote_path varchar(500) not null,
    local_path varchar(500) not null,
    file_name_pattern varchar(240) null,
    passive_mode_yn tinyint(1) not null default 1,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_file_transfer_config_definition (interface_definition_id),
    key idx_file_transfer_config_protocol (transfer_protocol),
    constraint fk_file_transfer_config_definition foreign key (interface_definition_id) references interface_definition (id)
);

create table batch_job_config (
    id bigint not null auto_increment,
    interface_definition_id bigint not null,
    job_name varchar(160) not null,
    cron_expression varchar(120) null,
    enabled_yn tinyint(1) not null default 0,
    max_parallel_count int not null default 1,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_batch_job_config_definition (interface_definition_id),
    unique key uk_batch_job_config_job_name (job_name),
    constraint fk_batch_job_config_definition foreign key (interface_definition_id) references interface_definition (id)
);

alter table batch_job_config
    add column job_type varchar(80) null after job_name,
    add column parameter_template_json longtext null after cron_expression,
    add column retryable_yn tinyint(1) not null default 1 after parameter_template_json,
    add column timeout_millis int not null default 300000 after retryable_yn,
    add column active_yn tinyint(1) not null default 1 after timeout_millis;

update batch_job_config
set job_type = 'INTERFACE_SETTLEMENT_SUMMARY',
    parameter_template_json = coalesce(parameter_template_json, '{"businessDate":"TODAY","forceFail":false}'),
    retryable_yn = coalesce(retryable_yn, 1),
    timeout_millis = coalesce(timeout_millis, 300000),
    active_yn = coalesce(active_yn, 1)
where job_type is null;

alter table batch_job_config
    modify column job_type varchar(80) not null;

create index idx_batch_job_config_active
    on batch_job_config (active_yn);

create index idx_batch_job_config_schedule
    on batch_job_config (active_yn, enabled_yn);

create table batch_run_history (
    id bigint not null auto_increment,
    interface_execution_id bigint not null,
    interface_definition_id bigint not null,
    batch_job_config_id bigint not null,
    spring_batch_job_execution_id bigint null,
    job_name varchar(160) not null,
    job_type varchar(80) not null,
    job_parameters_json longtext null,
    batch_status varchar(40) not null,
    exit_code varchar(80) null,
    exit_description varchar(2500) null,
    read_count bigint null,
    write_count bigint null,
    skip_count bigint null,
    started_at datetime(6) null,
    finished_at datetime(6) null,
    latency_ms bigint null,
    error_message varchar(2000) null,
    output_summary varchar(2000) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    key idx_batch_run_history_execution (interface_execution_id),
    key idx_batch_run_history_definition (interface_definition_id),
    key idx_batch_run_history_config (batch_job_config_id),
    key idx_batch_run_history_status (batch_status),
    key idx_batch_run_history_created_at (created_at),
    constraint fk_batch_run_history_execution foreign key (interface_execution_id) references interface_execution (id),
    constraint fk_batch_run_history_definition foreign key (interface_definition_id) references interface_definition (id),
    constraint fk_batch_run_history_config foreign key (batch_job_config_id) references batch_job_config (id)
);

create table batch_step_history (
    id bigint not null auto_increment,
    batch_run_history_id bigint not null,
    step_name varchar(160) not null,
    step_status varchar(40) not null,
    read_count bigint null,
    write_count bigint null,
    commit_count bigint null,
    rollback_count bigint null,
    skip_count bigint null,
    started_at datetime(6) null,
    finished_at datetime(6) null,
    exit_code varchar(80) null,
    exit_description varchar(2500) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    key idx_batch_step_history_run (batch_run_history_id),
    key idx_batch_step_history_status (step_status),
    constraint fk_batch_step_history_run foreign key (batch_run_history_id) references batch_run_history (id)
);

create table if not exists BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID bigint not null primary key,
    VERSION bigint null,
    JOB_NAME varchar(100) not null,
    JOB_KEY varchar(32) not null,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) engine=InnoDB;

create table if not exists BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID bigint not null primary key,
    VERSION bigint null,
    JOB_INSTANCE_ID bigint not null,
    CREATE_TIME datetime(6) not null,
    START_TIME datetime(6) default null,
    END_TIME datetime(6) default null,
    STATUS varchar(10) null,
    EXIT_CODE varchar(2500) null,
    EXIT_MESSAGE varchar(2500) null,
    LAST_UPDATED datetime(6) null,
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID) references BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
) engine=InnoDB;

create table if not exists BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID bigint not null,
    PARAMETER_NAME varchar(100) not null,
    PARAMETER_TYPE varchar(100) not null,
    PARAMETER_VALUE varchar(2500) null,
    IDENTIFYING char(1) not null,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) engine=InnoDB;

create table if not exists BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID bigint not null primary key,
    VERSION bigint not null,
    STEP_NAME varchar(100) not null,
    JOB_EXECUTION_ID bigint not null,
    CREATE_TIME datetime(6) not null,
    START_TIME datetime(6) default null,
    END_TIME datetime(6) default null,
    STATUS varchar(10) null,
    COMMIT_COUNT bigint null,
    READ_COUNT bigint null,
    FILTER_COUNT bigint null,
    WRITE_COUNT bigint null,
    READ_SKIP_COUNT bigint null,
    WRITE_SKIP_COUNT bigint null,
    PROCESS_SKIP_COUNT bigint null,
    ROLLBACK_COUNT bigint null,
    EXIT_CODE varchar(2500) null,
    EXIT_MESSAGE varchar(2500) null,
    LAST_UPDATED datetime(6) null,
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) engine=InnoDB;

create table if not exists BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID bigint not null primary key,
    SHORT_CONTEXT varchar(2500) not null,
    SERIALIZED_CONTEXT text null,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID) references BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
) engine=InnoDB;

create table if not exists BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID bigint not null primary key,
    SHORT_CONTEXT varchar(2500) not null,
    SERIALIZED_CONTEXT text null,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) engine=InnoDB;

create table if not exists BATCH_STEP_EXECUTION_SEQ (
    ID bigint not null
) engine=InnoDB;

create table if not exists BATCH_JOB_EXECUTION_SEQ (
    ID bigint not null
) engine=InnoDB;

create table if not exists BATCH_JOB_SEQ (
    ID bigint not null
) engine=InnoDB;

insert into BATCH_STEP_EXECUTION_SEQ (ID)
select 0
where not exists (select 1 from BATCH_STEP_EXECUTION_SEQ);

insert into BATCH_JOB_EXECUTION_SEQ (ID)
select 0
where not exists (select 1 from BATCH_JOB_EXECUTION_SEQ);

insert into BATCH_JOB_SEQ (ID)
select 0
where not exists (select 1 from BATCH_JOB_SEQ);

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
    'IF_BATCH_SETTLEMENT_001',
    'Daily interface settlement summary batch',
    'Sample Phase 7 batch interface that summarizes today''s executions by protocol and status',
    'BATCH',
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
      select 1 from interface_definition where interface_code = 'IF_BATCH_SETTLEMENT_001'
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
    'IF_BATCH_RETRY_AGG_001',
    'Failed execution retry aggregation batch',
    'Sample Phase 7 batch interface that aggregates failed executions and pending retry tasks',
    'BATCH',
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
      select 1 from interface_definition where interface_code = 'IF_BATCH_RETRY_AGG_001'
  );

insert into batch_job_config (
    interface_definition_id,
    job_name,
    job_type,
    cron_expression,
    parameter_template_json,
    enabled_yn,
    max_parallel_count,
    retryable_yn,
    timeout_millis,
    active_yn
)
select
    d.id,
    'interfaceSettlementSummaryJob',
    'INTERFACE_SETTLEMENT_SUMMARY',
    '0/30 * * * * *',
    '{"businessDate":"TODAY","forceFail":false}',
    0,
    1,
    1,
    300000,
    1
from interface_definition d
where d.interface_code = 'IF_BATCH_SETTLEMENT_001'
  and not exists (
      select 1 from batch_job_config c where c.interface_definition_id = d.id
  );

insert into batch_job_config (
    interface_definition_id,
    job_name,
    job_type,
    cron_expression,
    parameter_template_json,
    enabled_yn,
    max_parallel_count,
    retryable_yn,
    timeout_millis,
    active_yn
)
select
    d.id,
    'failedExecutionRetryAggregationJob',
    'FAILED_RETRY_AGGREGATION',
    '0/30 * * * * *',
    '{"businessDate":"TODAY","forceFail":false}',
    0,
    1,
    1,
    300000,
    1
from interface_definition d
where d.interface_code = 'IF_BATCH_RETRY_AGG_001'
  and not exists (
      select 1 from batch_job_config c where c.interface_definition_id = d.id
  );

alter table interface_execution
    add column execution_no varchar(120) null after id,
    add column retry_source_execution_id bigint null after interface_definition_id,
    add column protocol_type varchar(30) null after retry_source_execution_id,
    add column request_payload longtext null after requested_by,
    add column response_payload longtext null after request_payload;

update interface_execution e
join interface_definition d on d.id = e.interface_definition_id
set e.execution_no = e.execution_key,
    e.protocol_type = d.protocol_type
where e.execution_no is null;

update interface_execution
set execution_no = concat('LEGACY-', id)
where execution_no is null;

update interface_execution
set protocol_type = 'REST'
where protocol_type is null;

alter table interface_execution
    modify column execution_no varchar(120) not null,
    modify column protocol_type varchar(30) not null;

create unique index uk_interface_execution_no on interface_execution (execution_no);
create index idx_interface_execution_protocol on interface_execution (protocol_type);
create index idx_interface_execution_retry_source on interface_execution (retry_source_execution_id);

alter table interface_execution
    add constraint fk_interface_execution_retry_source
        foreign key (retry_source_execution_id) references interface_execution (id);

alter table interface_retry_task
    add column last_retried_at datetime(6) null after retry_count;

create index idx_interface_retry_task_execution_status
    on interface_retry_task (interface_execution_id, retry_status);

alter table admin_user
    add column password_hash varchar(100) null after display_name,
    add column description varchar(500) null after status;

alter table partner_company
    add column status varchar(30) not null default 'ACTIVE' after partner_name,
    add column description varchar(1000) null after status;

update partner_company
set status = case when active_yn = 1 then 'ACTIVE' else 'INACTIVE' end;

alter table internal_system
    add column status varchar(30) not null default 'ACTIVE' after owner_department,
    add column description varchar(1000) null after status;

update internal_system
set status = case when active_yn = 1 then 'ACTIVE' else 'INACTIVE' end;

alter table interface_definition
    add column status varchar(30) not null default 'ACTIVE' after direction_type;

update interface_definition
set status = case when enabled_yn = 1 then 'ACTIVE' else 'INACTIVE' end;

create index idx_partner_company_status on partner_company (status);
create index idx_internal_system_status on internal_system (status);
create index idx_interface_definition_status on interface_definition (status);

insert into admin_user (
    login_id,
    password_hash,
    display_name,
    email,
    role_code,
    status,
    description
)
select
    'admin',
    '$2a$10$XHz8tEnTe/FwPffF80cEEu4tPHXFopWqrQEpQwn4pfH5PjE5PbExa',
    'Local Demo Admin',
    'admin@local.insurancehub',
    'ROLE_ADMIN',
    'ACTIVE',
    'Seeded local demo account for Phase 1'
where not exists (
    select 1 from admin_user where login_id = 'admin'
);

insert into partner_company (
    partner_code,
    partner_name,
    status,
    description
)
select
    'LIFEPLUS',
    'Life Plus Insurance',
    'ACTIVE',
    'Sample partner company for local Phase 1 demos'
where not exists (
    select 1 from partner_company where partner_code = 'LIFEPLUS'
);

insert into internal_system (
    system_code,
    system_name,
    owner_department,
    status,
    description
)
select
    'POLICY_CORE',
    'Policy Core System',
    'Insurance Platform Team',
    'ACTIVE',
    'Sample internal source system for local Phase 1 demos'
where not exists (
    select 1 from internal_system where system_code = 'POLICY_CORE'
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
    'IF_REST_POLICY_001',
    'Policy status outbound REST interface',
    'Sample interface definition for Phase 1 CRUD demos',
    'REST',
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
      select 1 from interface_definition where interface_code = 'IF_REST_POLICY_001'
  );

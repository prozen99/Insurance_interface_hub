alter table soap_endpoint_config
    add column operation_name varchar(160) null after soap_action,
    add column request_template_xml longtext null after namespace_uri,
    add column active_yn tinyint(1) not null default 1 after timeout_millis;

alter table interface_execution
    add column protocol_action varchar(300) null after request_method;

create index idx_soap_endpoint_config_active
    on soap_endpoint_config (active_yn);

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
    'IF_SOAP_POLICY_001',
    'Policy inquiry outbound SOAP interface',
    'Sample SOAP interface for Phase 4 real SOAP execution demos',
    'SOAP',
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
      select 1 from interface_definition where interface_code = 'IF_SOAP_POLICY_001'
  );

insert into soap_endpoint_config (
    interface_definition_id,
    service_url,
    soap_action,
    operation_name,
    namespace_uri,
    request_template_xml,
    timeout_millis,
    active_yn
)
select
    d.id,
    'http://localhost:8080/simulator/soap/policy-inquiry',
    'urn:PolicyInquiry',
    'PolicyInquiry',
    'http://insurancehub.local/soap/policy',
    '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:pol="http://insurancehub.local/soap/policy"><soapenv:Header/><soapenv:Body><pol:PolicyInquiryRequest><pol:policyNo>POL-001</pol:policyNo></pol:PolicyInquiryRequest></soapenv:Body></soapenv:Envelope>',
    5000,
    1
from interface_definition d
where d.interface_code = 'IF_SOAP_POLICY_001'
  and not exists (
      select 1
      from soap_endpoint_config c
      where c.interface_definition_id = d.id
  );

update soap_endpoint_config c
join interface_definition d on d.id = c.interface_definition_id
set c.service_url = coalesce(c.service_url, 'http://localhost:8080/simulator/soap/policy-inquiry'),
    c.soap_action = coalesce(c.soap_action, 'urn:PolicyInquiry'),
    c.operation_name = coalesce(c.operation_name, 'PolicyInquiry'),
    c.namespace_uri = coalesce(c.namespace_uri, 'http://insurancehub.local/soap/policy'),
    c.request_template_xml = coalesce(c.request_template_xml, '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:pol="http://insurancehub.local/soap/policy"><soapenv:Header/><soapenv:Body><pol:PolicyInquiryRequest><pol:policyNo>POL-001</pol:policyNo></pol:PolicyInquiryRequest></soapenv:Body></soapenv:Envelope>'),
    c.timeout_millis = coalesce(c.timeout_millis, 5000),
    c.active_yn = 1
where d.interface_code = 'IF_SOAP_POLICY_001';

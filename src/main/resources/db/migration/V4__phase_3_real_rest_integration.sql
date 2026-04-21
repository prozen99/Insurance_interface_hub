alter table rest_endpoint_config
    add column base_url varchar(500) null after endpoint_url,
    add column path varchar(300) null after base_url,
    add column headers_json longtext null after auth_type,
    add column sample_request_body longtext null after headers_json,
    add column active_yn tinyint(1) not null default 1 after sample_request_body;

update rest_endpoint_config
set base_url = case
        when endpoint_url like 'http://localhost:8080/%' then 'http://localhost:8080'
        when endpoint_url like 'https://localhost:8080/%' then 'https://localhost:8080'
        else base_url
    end,
    path = case
        when endpoint_url like 'http://localhost:8080/%' then concat('/', substring(endpoint_url, length('http://localhost:8080/') + 1))
        when endpoint_url like 'https://localhost:8080/%' then concat('/', substring(endpoint_url, length('https://localhost:8080/') + 1))
        when endpoint_url like '/%' then endpoint_url
        else path
    end
where base_url is null
   or path is null;

update rest_endpoint_config
set base_url = 'http://localhost:8080'
where base_url is null;

update rest_endpoint_config
set path = '/simulator/rest/premium/calculate'
where path is null;

update rest_endpoint_config
set endpoint_url = concat(
        trim(trailing '/' from base_url),
        case when path like '/%' then path else concat('/', path) end
    )
where endpoint_url is null
   or endpoint_url = '';

alter table interface_execution
    add column request_url varchar(1000) null after request_payload,
    add column request_method varchar(20) null after request_url,
    add column request_headers longtext null after request_method,
    add column response_status_code int null after response_payload,
    add column response_headers longtext null after response_status_code,
    add column latency_ms bigint null after response_headers;

create index idx_rest_endpoint_config_active
    on rest_endpoint_config (active_yn);

create index idx_interface_execution_response_status
    on interface_execution (response_status_code);

insert into rest_endpoint_config (
    interface_definition_id,
    http_method,
    endpoint_url,
    base_url,
    path,
    timeout_millis,
    auth_type,
    headers_json,
    sample_request_body,
    active_yn
)
select
    d.id,
    'POST',
    'http://localhost:8080/simulator/rest/premium/calculate',
    'http://localhost:8080',
    '/simulator/rest/premium/calculate',
    3000,
    'NONE',
    '{"Content-Type":"application/json"}',
    '{"policyNo":"P001","age":42,"coverageAmount":100000000}',
    1
from interface_definition d
where d.interface_code = 'IF_REST_POLICY_001'
  and not exists (
      select 1
      from rest_endpoint_config c
      where c.interface_definition_id = d.id
  );

update rest_endpoint_config c
join interface_definition d on d.id = c.interface_definition_id
set c.base_url = coalesce(c.base_url, 'http://localhost:8080'),
    c.path = coalesce(c.path, '/simulator/rest/premium/calculate'),
    c.endpoint_url = 'http://localhost:8080/simulator/rest/premium/calculate',
    c.headers_json = coalesce(c.headers_json, '{"Content-Type":"application/json"}'),
    c.sample_request_body = coalesce(c.sample_request_body, '{"policyNo":"P001","age":42,"coverageAmount":100000000}'),
    c.active_yn = 1
where d.interface_code = 'IF_REST_POLICY_001';

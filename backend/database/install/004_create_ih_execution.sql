
create table ih_execution (
    id number not null,

    integration_id number,
    integration_name varchar2(100),

    endpoint_id number,
    endpoint_name varchar2(100),

    credential_id number,
    credential_name varchar2(100),

    request_method varchar2(10) not null,
    request_path varchar2(500) not null,
    request_parameters clob,

    started_at timestamp default current_timestamp not null,
    finished_at timestamp,
    duration_ms number(12),

    http_status number(3),
    execution_status varchar2(20) not null,

    error_type varchar2(100),
    error_message varchar2(2000),

    created_at timestamp default current_timestamp not null,

    constraint pk_ih_execution
        primary key (id),

    constraint ck_ih_execution_status
        check (execution_status in (
            'SUCCESS',
            'ERROR',
            'UNAUTHORIZED',
            'NOT_FOUND'
        )),

    constraint ck_ih_execution_duration
        check (duration_ms >= 0),

    constraint ck_ih_execution_http_status
        check (http_status between 100 and 599)
);

create sequence ih_execution_seq
    start with 1
    increment by 1
    nocache
    nocycle;

create index ix_ih_execution_started_at
    on ih_execution (started_at);

create index ix_ih_execution_integration
    on ih_execution (integration_id, started_at);

create index ix_ih_execution_endpoint
    on ih_execution (endpoint_id, started_at);

create index ix_ih_execution_credential
    on ih_execution (credential_id, started_at);

create index ix_ih_execution_status
    on ih_execution (execution_status, started_at);

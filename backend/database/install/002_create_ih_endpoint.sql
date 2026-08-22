create table ih_endpoint (
    id number not null,
    integration_id number not null,
    name varchar2(100) not null,
    description varchar2(500),
    path varchar2(200) not null,
    method varchar2(10) not null,
    sql_text clob not null,
    parameters clob,
    active char(1) default 'S' not null,
    created_by varchar2(100) default 'SYSTEM' not null,
    created_at timestamp default current_timestamp not null,
    updated_by varchar2(100),
    updated_at timestamp,

    constraint pk_ih_endpoint
        primary key (id),

    constraint fk_ih_endpoint_integration
        foreign key (integration_id)
        references ih_integration (id),

    constraint uk_ih_endpoint_route
        unique (integration_id, path, method),

    constraint ck_ih_endpoint_active
        check (active in ('S', 'N')),

    constraint ck_ih_endpoint_method
        check (method in ('GET'))
);

create sequence ih_endpoint_seq
    start with 1
    increment by 1
    nocache
    nocycle;
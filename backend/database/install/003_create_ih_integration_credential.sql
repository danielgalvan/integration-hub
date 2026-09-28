create table ih_integration_credential (
    id number not null,
    integration_id number not null,
    name varchar2(100) not null,
    api_key_encrypted varchar2(1000) not null,
    active char(1) default 'S' not null,
    last_used_at timestamp,

    created_by varchar2(100) default 'SYSTEM' not null,
    created_at timestamp default current_timestamp not null,
    updated_by varchar2(100),
    updated_at timestamp,

    constraint pk_ih_integration_credential
        primary key (id),

    constraint fk_ih_integration_credential_int
        foreign key (integration_id)
        references ih_integration (id),

    constraint uk_ih_integration_cred_name
        unique (integration_id, name),

    constraint ck_ih_integration_cred_active
        check (active in ('S', 'N'))
);

create sequence ih_integration_credential_seq
    start with 1
    increment by 1
    nocache
    nocycle;

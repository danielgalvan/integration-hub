create table ih_integration (
    id number not null,
    name varchar2(100) not null,
    description varchar2(500),
    base_path varchar2(200) not null,
    active char(1) default 'S' not null,
    created_by varchar2(100) default 'SYSTEM' not null,
    created_at timestamp default current_timestamp not null,
    updated_by varchar2(100),
    updated_at timestamp,

    constraint pk_ih_integration
        primary key (id),

    constraint uk_ih_integration_base_path
        unique (base_path),

    constraint ck_ih_integration_active
        check (active in ('S', 'N'))
);

create sequence ih_integration_seq
    start with 1
    increment by 1
    nocache
    nocycle;
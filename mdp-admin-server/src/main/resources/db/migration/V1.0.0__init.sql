create schema if not exists public;

comment on schema public is 'standard public schema';

alter schema public owner to agens;

CREATE EXTENSION IF NOT EXISTS zhparser WITH SCHEMA public;

create user docker;

--
-- Name: zhcfg; Type: TEXT SEARCH CONFIGURATION; Schema: public; Owner: docker
--

CREATE TEXT SEARCH CONFIGURATION public.zhcfg (
    PARSER = public.zhparser );

ALTER TEXT SEARCH CONFIGURATION public.zhcfg
    ADD MAPPING FOR a WITH simple;

ALTER TEXT SEARCH CONFIGURATION public.zhcfg
    ADD MAPPING FOR e WITH simple;

ALTER TEXT SEARCH CONFIGURATION public.zhcfg
    ADD MAPPING FOR i WITH simple;

ALTER TEXT SEARCH CONFIGURATION public.zhcfg
    ADD MAPPING FOR l WITH simple;

ALTER TEXT SEARCH CONFIGURATION public.zhcfg
    ADD MAPPING FOR n WITH simple;

ALTER TEXT SEARCH CONFIGURATION public.zhcfg
    ADD MAPPING FOR v WITH simple;


ALTER TEXT SEARCH CONFIGURATION public.zhcfg OWNER TO docker;

CREATE EXTENSION IF NOT EXISTS postgres_fdw WITH SCHEMA public;

--
-- Name: metabase_db; Type: SERVER; Schema: -; Owner: agens
--

CREATE SERVER metabase_db FOREIGN DATA WRAPPER postgres_fdw OPTIONS (
    dbname 'metabase',
    host '10.0.1.254'
    );

ALTER SERVER metabase_db OWNER TO agens;

--
-- Name: USER MAPPING agens SERVER metabase_db; Type: USER MAPPING; Schema: -; Owner: agens
--
CREATE USER MAPPING FOR agens SERVER metabase_db OPTIONS (
    password 'docker',
    "user" 'docker'
    );
--
-- Name: USER MAPPING docker SERVER metabase_db; Type: USER MAPPING; Schema: -; Owner: agens
--
CREATE USER MAPPING FOR docker SERVER metabase_db OPTIONS (
    password 'docker',
    "user" 'docker'
    );

CREATE SCHEMA IF NOT EXISTS metabase;

ALTER SCHEMA metabase OWNER TO agens;

CREATE FOREIGN TABLE metabase.metabase_database (
    id integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    name character varying(254) NOT NULL,
    description text,
    details text,
    engine character varying(254) NOT NULL,
    is_sample boolean NOT NULL,
    is_full_sync boolean NOT NULL,
    points_of_interest text,
    caveats text,
    metadata_sync_schedule character varying(254) NOT NULL,
    cache_field_values_schedule character varying(254) NOT NULL,
    timezone character varying(254),
    is_on_demand boolean NOT NULL,
    options text,
    auto_run_queries boolean NOT NULL
    )
    SERVER metabase_db
    OPTIONS (
    schema_name 'public',
    table_name 'metabase_database'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN id OPTIONS (
    column_name 'id'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN created_at OPTIONS (
    column_name 'created_at'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN updated_at OPTIONS (
    column_name 'updated_at'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN name OPTIONS (
    column_name 'name'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN description OPTIONS (
    column_name 'description'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN details OPTIONS (
    column_name 'details'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN engine OPTIONS (
    column_name 'engine'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN is_sample OPTIONS (
    column_name 'is_sample'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN is_full_sync OPTIONS (
    column_name 'is_full_sync'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN points_of_interest OPTIONS (
    column_name 'points_of_interest'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN caveats OPTIONS (
    column_name 'caveats'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN metadata_sync_schedule OPTIONS (
    column_name 'metadata_sync_schedule'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN cache_field_values_schedule OPTIONS (
    column_name 'cache_field_values_schedule'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN timezone OPTIONS (
    column_name 'timezone'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN is_on_demand OPTIONS (
    column_name 'is_on_demand'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN options OPTIONS (
    column_name 'options'
    );
ALTER FOREIGN TABLE metabase.metabase_database ALTER COLUMN auto_run_queries OPTIONS (
    column_name 'auto_run_queries'
    );

ALTER FOREIGN TABLE metabase.metabase_database OWNER TO agens;

create table meta_database
(
    id serial not null
        constraint meta_database_pkey
            primary key,
    create_time timestamp with time zone not null,
    update_time timestamp with time zone not null,
    name varchar(254) not null,
    description text,
    details json,
    engine varchar(254) not null,
    is_full_sync boolean default true not null,
    metadata_sync_schedule varchar(254) default '0 50 * * * ? *'::character varying not null,
    cache_field_values_schedule varchar(254) default '0 50 0 * * ? *'::character varying not null,
    timezone varchar(254),
    options text
);

comment on column meta_database.metadata_sync_schedule is 'The cron schedule string for when this database should undergo the metadata sync process (and analysis for new fields).';

comment on column meta_database.cache_field_values_schedule is 'The cron schedule string for when FieldValues for eligible Fields should be updated.';

comment on column meta_database.timezone is 'Timezone identifier for the database, set by the sync process';

comment on column meta_database.options is 'Serialized JSON containing various options like QB behavior.';

alter table meta_database owner to docker;

create table tags
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint tags_pkey
            primary key,
    name varchar(500),
    color varchar(500)
);

alter table tags owner to docker;

create table flow_tags_ref
(
    flow_id varchar(50),
    tag_id varchar(50)
        constraint flow_tags_ref_tags_id_fk
            references tags
);

alter table flow_tags_ref owner to docker;

create table operator_tags_ref
(
    operator_id varchar(50) not null,
    tag_id varchar(50) not null
        constraint operator_tags_ref_tags_id_fk
            references tags,
    constraint task_def_tags_ref_pk
        primary key (operator_id, tag_id)
);

alter table operator_tags_ref owner to docker;

create index operator_tags_ref_operator_id_index
    on operator_tags_ref (operator_id);

create unique index tag_name
    on tags (name);

create table user_session
(
    primary_id char(36) not null
        constraint user_session_pk
            primary key,
    session_id char(36) not null,
    creation_time bigint not null,
    last_access_time bigint not null,
    max_inactive_interval integer not null,
    expiry_time bigint not null,
    principal_name varchar(100)
);

alter table user_session owner to docker;

create unique index user_session_ix1
    on user_session (session_id);

create index user_session_ix2
    on user_session (expiry_time);

create index user_session_ix3
    on user_session (principal_name);

create table user_session_attributes
(
    session_primary_id char(36) not null
        constraint user_session_attributes_fk
            references user_session
            on delete cascade,
    attribute_name varchar(200) not null,
    attribute_bytes bytea not null,
    constraint user_session_attributes_pk
        primary key (session_primary_id, attribute_name)
);

alter table user_session_attributes owner to docker;

create table users
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint users_pkey
            primary key,
    user_name varchar(200),
    user_info json,
    last_login timestamp with time zone,
    is_active boolean,
    create_time timestamp with time zone,
    update_time timestamp with time zone
);

alter table users owner to docker;

create table audit_log
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint audit_log_pkey
            primary key,
    topic varchar(32) not null,
    timestamp timestamp with time zone not null,
    user_id varchar(50)
        constraint audit_log_users_id_fk
            references users,
    model varchar(16),
    model_id varchar(50),
    custom_id varchar(48),
    details text not null
);

alter table audit_log owner to docker;

create table flow
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint task_dag_pkey
            primary key,
    name varchar(500),
    dag_config json,
    description text,
    airflow_address text,
    user_ids varchar(50) [],
    create_time timestamp with time zone,
    update_time timestamp with time zone,
    creator_id varchar(50)
        constraint flow_users_id_fk
            references users,
    execution_scheduler varchar(254),
    retry_policy integer,
    enable_scheduler boolean,
    deleted integer default 0,
    enable_parallel boolean default false,
    tag_ids varchar(50) [],
    "_ts_query" tsvector
);

alter table flow owner to docker;

create table flow_run
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint dag_run_pkey
            primary key,
    state varchar(200),
    conf json,
    flow_id varchar(50)
        constraint flow_run_flow_id_fk
            references flow,
    last_running_stage integer,
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    create_time timestamp with time zone,
    update_time timestamp with time zone,
    deleted integer default 0,
    creator_id varchar(50)
);

alter table flow_run owner to docker;

create index flow_rum_query
    on flow ("_ts_query");

CREATE TRIGGER flow_trigger_update__ts_query
    BEFORE INSERT OR UPDATE ON public.flow
    FOR EACH ROW EXECUTE
    PROCEDURE tsvector_update_trigger('_ts_query', 'public.zhcfg', 'name', 'description');

create table meta_table
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint meta_table_pkey
            primary key,
    name varchar(500),
    table_meta json,
    db_id integer,
    create_time timestamp with time zone,
    update_time timestamp with time zone,
    source_config json,
    version integer,
    creator_id varchar(50)
        constraint meta_table_users_id_fk
            references users,
    doc_url varchar(500),
    description text,
    user_ids varchar(50) [],
    is_archived boolean,
    lifecycle varchar(200),
    is_active boolean,
    schema varchar(200),
    ref_db_ids integer[],
    database_type varchar(200),
    tag_ids varchar(50) [] default '{}'::character varying[],
    constraint meta_table_pk
        unique (database_type, schema, name)
);

alter table meta_table owner to docker;

create table meta_field
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint metabase_field_pkey
            primary key,
    create_time timestamp with time zone not null,
    update_time timestamp with time zone not null,
    name varchar(254) not null,
    base_type varchar(255),
    special_type varchar(255),
    is_active boolean default true not null,
    description text,
    position integer default 0 not null,
    table_id varchar(50) not null
        constraint meta_field_meta_table_id_fk
            references meta_table,
    parent_id varchar(50)
        constraint meta_field_meta_field_id_fk
            references meta_field,
    visibility_type varchar(32) default 'normal'::character varying,
    fk_target_field_id integer,
    last_analyzed timestamp with time zone,
    fingerprint text,
    fingerprint_version integer default 0 not null,
    database_type text not null,
    has_field_values text,
    settings text,
    is_key_field boolean default false,
    is_nullable boolean default true,
    is_unique boolean default false,
    constraint idx_uniq_field_table_id_parent_id_name
        unique (table_id, parent_id, name)
);

comment on column meta_field.parent_id is 'parent column id';

comment on column meta_field.fingerprint is 'Serialized JSON containing non-identifying information about this Field, such as min, max, and percent JSON. Used for classification.';

comment on column meta_field.fingerprint_version is 'The version of the fingerprint for this Field. Used so we can keep track of which Fields need to be analyzed again when new things are added to fingerprints.';

comment on column meta_field.database_type is 'The actual type of this column in the database. e.g. VARCHAR or TEXT.';

comment on column meta_field.has_field_values is 'Whether we have FieldValues ("list"), should ad-hoc search ("search"), disable entirely ("none"), or infer dynamically (null)"';

comment on column meta_field.settings is 'Serialized JSON FE-specific settings like formatting, etc. Scope of what is stored here may increase in future.';

alter table meta_field owner to docker;

create index meta_field_table_id_index
    on meta_field (table_id);

create table meta_table_revision
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint meta_table_revision_pkey
            primary key,
    details text not null,
    table_id varchar(50) not null
        constraint meta_table_revision_meta_table_id_fk
            references meta_table,
    create_time timestamp with time zone not null,
    remark text
);

comment on table meta_table_revision is 'Used to keep track of changes made to table.';

comment on column meta_table_revision.details is 'Serialized JSON of the changes.';

comment on column meta_table_revision.create_time is 'The timestamp of when these changes were made.';

alter table meta_table_revision owner to docker;

create table meta_table_tags_ref
(
    table_id varchar(50)
        constraint meta_table_tags_ref_meta_table_id_fk
            references meta_table,
    tag_id varchar(50)
        constraint meta_table_tags_ref_tags_id_fk
            references tags,
    constraint table_tags_ref_pk
        unique (table_id, tag_id)
);

alter table meta_table_tags_ref owner to docker;

create table schemas
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint schemas_pkey
            primary key,
    commit varchar(200),
    name varchar(200),
    type varchar(200),
    payload json,
    is_latest boolean,
    create_time timestamp with time zone,
    update_time timestamp with time zone,
    creator_id varchar(50)
        constraint schemas_users_id_fk
            references users,
    constraint unique_name_commit
        unique (commit, name)
);

alter table schemas owner to docker;

create unique index user_name
    on users (user_name);

create table data_test_case
(
    id varchar(50) not null
        constraint data_test_case_pkey
            primary key,
    create_time timestamp,
    update_time timestamp,
    case_sql text,
    description text,
    name text,
    validate_object jsonb,
    db_id integer,
    tags_ids varchar(50) [],
    fields text[]
);

alter table data_test_case owner to docker;

create table flow_tasks
(
    -- Only integer types can be auto increment
    id varchar(50) not null
        constraint dag_tasks_pkey
            primary key,
    task_order integer,
    flow_id varchar(50)
        constraint flow_tasks_flow_id_fk
            references flow,
    create_time timestamp with time zone,
    update_time timestamp with time zone,
    test_id varchar(50)
        constraint flow_tasks_data_test_case_id_fk
            references data_test_case,
    trigger_rule varchar(200),
    arguments jsonb,
    operator_id varchar(50),
    name varchar(255),
    config jsonb,
    deleted integer default 0,
    creator_id varchar(50),
    retry_policy integer,
    parent_task_ids varchar(50) []
);

alter table flow_tasks owner to docker;

create table flow_tasks_ref
(
    source_task_id varchar(50)
        constraint flow_tasks_ref_flow_tasks_id_fk
            references flow_tasks,
    target_task_id varchar(50)
        constraint flow_tasks_ref_flow_tasks_id_fk_2
            references flow_tasks
);

alter table flow_tasks_ref owner to docker;

create unique index flow_tasks_ref_source_task_id_target_task_id_uindex
    on flow_tasks_ref (source_task_id, target_task_id);

create table meta_table_test_ref
(
    table_id varchar(50) not null
        constraint meta_table_test_ref_meta_table_id_fk
            references meta_table,
    test_id varchar(50) not null
        constraint meta_table_test_ref_data_test_case_id_fk
            references data_test_case,
    constraint table_test_ref_pkey
        primary key (table_id, test_id)
);

alter table meta_table_test_ref owner to docker;

create table data_test_result
(
    id varchar(50) not null
        constraint data_test_result_pkey
            primary key,
    create_time timestamp,
    update_time timestamp,
    case_id varchar(50),
    error_catch_log text,
    passed boolean
);

alter table data_test_result owner to docker;

create table data_test_case_tags
(
    case_id varchar(50) not null
        constraint fkeskncgpecbtqrkoowx8xu3k5q
            references data_test_case
            on delete cascade,
    tag_id varchar(50) not null
        constraint fkhtmtg83e5rqusnad0h6di9je3
            references tags
);

alter table data_test_case_tags owner to docker;

create table operator
(
    id varchar(50) not null
        constraint operator_pkey
            primary key,
    name varchar(200),
    create_time timestamp with time zone,
    update_time timestamp with time zone,
    platform varchar(50),
    platform_config json,
    allow_putback boolean,
    parameter_execution_type varchar(20),
    creator_id varchar(50)
        constraint operators_users_id_fk
            references users,
    description text,
    deleted integer default 0,
    tag_ids varchar(50) [] default '{}'::character varying[]
);

alter table operator owner to docker;

create table config_parameters
(
    id varchar(50) not null
        constraint config_parameters_pkey
            primary key,
    parameter_name varchar(200),
    is_active boolean,
    create_time timestamp with time zone,
    update_time timestamp with time zone,
    parameter_key varchar(200),
    parameter_type varchar(20),
    choice_url varchar(500),
    creator_id varchar(50),
    default_value text,
    operator_id varchar(50)
        constraint config_parameters_operator_id_fk
            references operator,
    choices text[]
);

alter table config_parameters owner to docker;

create table tasks
(
    id varchar(255) not null
        constraint tasks_pkey
            primary key,
    name varchar(500),
    config json,
    error_reason text,
    state varchar(200),
    create_time timestamp with time zone,
    update_time timestamp with time zone,
    table_id varchar(50)
        constraint tasks_meta_table_id_fk
            references meta_table,
    end_time timestamp with time zone,
    info json,
    creator_id varchar(50)
        constraint tasks_users_id_fk
            references users,
    start_time timestamp with time zone,
    flow_task_id varchar(50)
        constraint tasks_flow_tasks_id_fk
            references flow_tasks,
    flow_run_id varchar(50)
        constraint tasks_flow_run_id_fk
            references flow_run,
    try_number integer,
    max_retry integer,
    flow_id varchar(50)
);

alter table tasks owner to docker;

create table meta_table_flow_task_ref
(
    flow_task_id varchar(50)
        constraint meta_table_flow_task_ref_flow_tasks_id_fk
            references flow_tasks,
    table_id varchar(50)
        constraint meta_table_flow_task_ref_meta_table_id_fk
            references meta_table
);

alter table meta_table_flow_task_ref owner to docker;

create table qrtz_job_details
(
    sched_name varchar(120) not null,
    job_name varchar(200) not null,
    job_group varchar(200) not null,
    description varchar(250),
    job_class_name varchar(250) not null,
    is_durable boolean not null,
    is_nonconcurrent boolean not null,
    is_update_data boolean not null,
    requests_recovery boolean not null,
    job_data bytea,
    constraint qrtz_job_details_pkey
        primary key (sched_name, job_name, job_group)
);

alter table qrtz_job_details owner to docker;

create index idx_qrtz_j_req_recovery
    on qrtz_job_details (sched_name, requests_recovery);

create index idx_qrtz_j_grp
    on qrtz_job_details (sched_name, job_group);

create table qrtz_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    job_name varchar(200) not null,
    job_group varchar(200) not null,
    description varchar(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state varchar(16) not null,
    trigger_type varchar(8) not null,
    start_time bigint not null,
    end_time bigint,
    calendar_name varchar(200),
    misfire_instr smallint,
    job_data bytea,
    constraint qrtz_triggers_pkey
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_triggers_sched_name_fkey
        foreign key (sched_name, job_name, job_group) references qrtz_job_details
);

alter table qrtz_triggers owner to docker;

create index idx_qrtz_t_j
    on qrtz_triggers (sched_name, job_name, job_group);

create index idx_qrtz_t_jg
    on qrtz_triggers (sched_name, job_group);

create index idx_qrtz_t_c
    on qrtz_triggers (sched_name, calendar_name);

create index idx_qrtz_t_g
    on qrtz_triggers (sched_name, trigger_group);

create index idx_qrtz_t_state
    on qrtz_triggers (sched_name, trigger_state);

create index idx_qrtz_t_n_state
    on qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);

create index idx_qrtz_t_n_g_state
    on qrtz_triggers (sched_name, trigger_group, trigger_state);

create index idx_qrtz_t_next_fire_time
    on qrtz_triggers (sched_name, next_fire_time);

create index idx_qrtz_t_nft_st
    on qrtz_triggers (sched_name, trigger_state, next_fire_time);

create index idx_qrtz_t_nft_misfire
    on qrtz_triggers (sched_name, misfire_instr, next_fire_time);

create index idx_qrtz_t_nft_st_misfire
    on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);

create index idx_qrtz_t_nft_st_misfire_grp
    on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);

create table qrtz_simple_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    repeat_count bigint not null,
    repeat_interval bigint not null,
    times_triggered bigint not null,
    constraint qrtz_simple_triggers_pkey
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_simple_triggers_sched_name_fkey
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

alter table qrtz_simple_triggers owner to docker;

create table qrtz_cron_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    cron_expression varchar(120) not null,
    time_zone_id varchar(80),
    constraint qrtz_cron_triggers_pkey
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_cron_triggers_sched_name_fkey
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

alter table qrtz_cron_triggers owner to docker;

create table qrtz_simprop_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    str_prop_1 varchar(512),
    str_prop_2 varchar(512),
    str_prop_3 varchar(512),
    int_prop_1 integer,
    int_prop_2 integer,
    long_prop_1 bigint,
    long_prop_2 bigint,
    dec_prop_1 numeric(13,4),
    dec_prop_2 numeric(13,4),
    bool_prop_1 boolean,
    bool_prop_2 boolean,
    constraint qrtz_simprop_triggers_pkey
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_simprop_triggers_sched_name_fkey
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

alter table qrtz_simprop_triggers owner to docker;

create table qrtz_blob_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    blob_data bytea,
    constraint qrtz_blob_triggers_pkey
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_blob_triggers_sched_name_fkey
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

alter table qrtz_blob_triggers owner to docker;

create table qrtz_calendars
(
    sched_name varchar(120) not null,
    calendar_name varchar(200) not null,
    calendar bytea not null,
    constraint qrtz_calendars_pkey
        primary key (sched_name, calendar_name)
);

alter table qrtz_calendars owner to docker;

create table qrtz_paused_trigger_grps
(
    sched_name varchar(120) not null,
    trigger_group varchar(200) not null,
    constraint qrtz_paused_trigger_grps_pkey
        primary key (sched_name, trigger_group)
);

alter table qrtz_paused_trigger_grps owner to docker;

create table qrtz_fired_triggers
(
    sched_name varchar(120) not null,
    entry_id varchar(95) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    instance_name varchar(200) not null,
    fired_time bigint not null,
    sched_time bigint not null,
    priority integer not null,
    state varchar(16) not null,
    job_name varchar(200),
    job_group varchar(200),
    is_nonconcurrent boolean,
    requests_recovery boolean,
    constraint qrtz_fired_triggers_pkey
        primary key (sched_name, entry_id)
);

alter table qrtz_fired_triggers owner to docker;

create index idx_qrtz_ft_trig_inst_name
    on qrtz_fired_triggers (sched_name, instance_name);

create index idx_qrtz_ft_inst_job_req_rcvry
    on qrtz_fired_triggers (sched_name, instance_name, requests_recovery);

create index idx_qrtz_ft_j_g
    on qrtz_fired_triggers (sched_name, job_name, job_group);

create index idx_qrtz_ft_jg
    on qrtz_fired_triggers (sched_name, job_group);

create index idx_qrtz_ft_t_g
    on qrtz_fired_triggers (sched_name, trigger_name, trigger_group);

create index idx_qrtz_ft_tg
    on qrtz_fired_triggers (sched_name, trigger_group);

create table qrtz_scheduler_state
(
    sched_name varchar(120) not null,
    instance_name varchar(200) not null,
    last_checkin_time bigint not null,
    checkin_interval bigint not null,
    constraint qrtz_scheduler_state_pkey
        primary key (sched_name, instance_name)
);

alter table qrtz_scheduler_state owner to docker;

create table qrtz_locks
(
    sched_name varchar(120) not null,
    lock_name varchar(40) not null,
    constraint qrtz_locks_pkey
        primary key (sched_name, lock_name)
);

alter table qrtz_locks owner to docker;

create table application_tables
(
    c1 text,
    c2 text
);

alter table application_tables owner to docker;

create table case_coverage
(
    covered_columns_num numeric,
    total_columns_num numeric,
    coverage numeric,
    tag_ids varchar(50) [],
    id varchar(50),
    create_time timestamp,
    update_time timestamp
);

alter table case_coverage owner to docker;

create table meta_table_metrics
(
    id varchar(50) not null
        constraint meta_table_metrics_pk
            primary key,
    create_time timestamp with time zone not null,
    update_time timestamp with time zone not null,
    table_id varchar(50) not null
        constraint table_id
            references meta_table,
    metric jsonb not null
);

alter table meta_table_metrics owner to docker;

create index meta_table_metrics_create_time_index
    on meta_table_metrics (create_time desc);

create index meta_table_metrics_table_id_index
    on meta_table_metrics (table_id);


create view metabase_database(id, created_at, updated_at, name, description, details, engine, is_full_sync, timezone) as
    SELECT metabase_database.id,
           metabase_database.created_at,
           metabase_database.updated_at,
           metabase_database.name,
           metabase_database.description,
           metabase_database.details,
           metabase_database.engine,
           metabase_database.is_full_sync,
           metabase_database.timezone
    FROM metabase.metabase_database
    UNION ALL
    SELECT meta_database.id,
           meta_database.create_time     AS created_at,
           meta_database.update_time     AS updated_at,
           meta_database.name,
           meta_database.description,
           (meta_database.details)::text AS details,
           meta_database.engine,
           meta_database.is_full_sync,
           meta_database.timezone
    FROM meta_database;

alter table metabase_database owner to docker;

create function array_contains(anyarray, anyarray) returns boolean
    language plpgsql
as $$
BEGIN
    return $1 @> $2;
END;
$$;

alter function array_contains(anyarray, anyarray) owner to docker;

create function ts_query_match(tsvector, text) returns boolean
    language plpgsql
as $$
BEGIN
    return $1 @@ to_tsquery($2);
END;
$$;

alter function ts_query_match(tsvector, text) owner to docker;

create function create_ts_query_column(tablename text, parsername text, ts_column text DEFAULT '_ts_query'::text, VARIADIC columnnames text[] DEFAULT '{}'::text[]) returns integer
    language plpgsql
as $$
declare
    quote_fields TEXT = '';
    trigger_fields TEXT = '';

BEGIN
    SELECT string_agg(format ('coalesce(%s, '''')' ,quote_ident(i)), '||'' ''||') INTO quote_fields
    FROM
        (select unnest(columnnames) as i) as t;
    SELECT  string_agg(format ('%s' ,quote_ident(i)), ',') INTO trigger_fields
    FROM
        (select unnest(columnnames) as i) as t;

    BEGIN
        EXECUTE FORMAT('ALTER TABLE %s DROP COLUMN %s', tablename, ts_column);
    EXCEPTION WHEN OTHERS THEN
    END;
    BEGIN
        EXECUTE FORMAT('DROP INDEX %s_rum_query', tablename);
    EXCEPTION WHEN OTHERS THEN
    END;
    BEGIN
        EXECUTE FORMAT('DROP TRIGGER %s_trigger_update_%s on %s', tablename, ts_column, tablename);
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FAILED TO EXECUTE: %', FORMAT('DROP TRIGGER %s_trigger_update_%s', tablename, ts_column);
    END;
    BEGIN
        EXECUTE FORMAT('DROP INDEX idx_%s_text_search', tablename);
    EXCEPTION WHEN OTHERS THEN
    END;
    EXECUTE FORMAT('ALTER TABLE %s ADD COLUMN %s tsvector', tablename, ts_column);
    EXECUTE FORMAT('UPDATE %s SET %s = to_tsvector(''%s'', %s)',tablename, ts_column, parsername, quote_fields);
    EXECUTE FORMAT('CREATE INDEX %s_rum_query ON %s USING GIN(%s)', tablename,tablename,ts_column);
    EXECUTE FORMAT('CREATE TRIGGER %s_trigger_update_%s ' ||
                   'BEFORE INSERT OR UPDATE ' ||
                   'ON %s FOR EACH ROW EXECUTE PROCEDURE ' ||
                   'tsvector_update_trigger(%s, ''%s'', %s)',
                   tablename, ts_column,tablename, ts_column, 'public.' || parsername, trigger_fields);

    RETURN 0;
END;
$$;

alter function create_ts_query_column(text, text, text, text[]) owner to docker;

create function record_to_text(anyelement) returns text
    immutable
    strict
    language sql
as $$
select $1::text;
$$;

alter function record_to_text(anyelement) owner to docker;

create function ts_text_match(tsvector, text) returns boolean
    language plpgsql
as $$
BEGIN
    return $1 @@ plainto_tsquery('zhcfg',$2);
END;
$$;

alter function ts_text_match(tsvector, text) owner to docker;

create function array_contains(anyarray, text) returns boolean
    language plpgsql
as $$
BEGIN
    return $1::text[] @> (regexp_split_to_array($2, ',' )::text[]);
END;
$$;

alter function array_contains(anyarray, text) owner to docker;


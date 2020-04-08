create database "mdp";

create unique index tag_name on public.tags (name);
create unique index user_name on public.users (user_name);

alter table public.activity rename to public.audit_log;
alter table public.audit_log rename constraint activity_pkey to audit_log_pkey;
alter index activity_pkey rename to audit_log_pkey;
alter table public.activity rename constraint activity_users_id_fk to audit_log_users_id_fk;



drop view if exists metabase_database;
create view metabase_database as
    select
        id, created_at, updated_at, name, description, details, engine, is_full_sync, timezone
    from metabase.metabase_database
    union all
    select
        id, create_time, update_time, name, description, details::text, engine, is_full_sync,timezone
    from meta_database;


alter table if exists public.data_test_result
    add column execute_result jsonb,
    add column impact_level varchar(50),
    add column template_operation varchar(50);

alter table if exists public.data_test_case
    add column is_block_flow bool,
    add column is_table_level bool,
    add column operation_field_ids varchar(100)[],
    add column owner_id varchar(50),
    add column quality_property varchar(50),
    add column rule_type varchar(50),
    add column template_operation varchar(50),
    add column template_type varchar(50);
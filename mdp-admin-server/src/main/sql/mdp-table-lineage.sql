create graph mdp_graph;
set graph_path = mdp_graph;

create vlabel meta_table;
create elabel meta_table_lineage;

create unique property index on meta_table (table_id);
create unique property index on meta_table_lineage (edge_id);
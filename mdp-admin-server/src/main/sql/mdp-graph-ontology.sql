create graph mdp_graph;
set graph_path = mdp_graph;

create vlabel graph_ontology_vertex;
create elabel graph_ontology_edge;

create unique property index on graph_ontology_vertex (vertex_id);
create unique property index on graph_ontology_edge (edge_id);
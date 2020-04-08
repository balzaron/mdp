package com.miotech.mdp.table.model.dao;

import javax.persistence.*;

@Entity
@Table(name = "metabase_table", schema = "metabase", catalog = "mdp")
public class MetabaseTableEntity {
    @Basic
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name="db_id")
    private Integer dbId;

    @Column(name="name")
    private String name;

    @Column(name="schema")
    private String schema;

}

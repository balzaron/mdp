package com.miotech.mdp.table.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miotech.mdp.common.model.BaseEntity;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.util.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "meta_table", schema = "public", catalog = "mdp")
@Transactional
@NamedEntityGraph(name = "table.graph", attributeNodes = {@NamedAttributeNode("tags")})
@Data
@NoArgsConstructor
public class MetaTableEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Type(type = "jsonb")
    @Column(name = "table_meta", columnDefinition = "json")
    private TableMeta tableMeta;

    @Column(name = "database_type")
    private String databaseType;

    @Column(name = "db_id")
    private Integer dbId;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "table")
    private List<MetaFieldEntity> fields = new ArrayList<>();

    @Type(type = "json")
    @Column(name = "source_config")
    private String sourceConfig;

    @Column(name = "version")
    private Integer version;

    @Column(name = "doc_url")
    private String docUrl;

    @Column(name = "description")
    private String description;

    @Type(type = "string-array")
    @Column(name = "user_ids", columnDefinition ="varchar(50) []")
    private String[] userIds;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Column(name = "lifecycle")
    private String lifecycle;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "schema")
    private String schema;

    @Type(type = "int-array")
    @Column(name = "ref_db_ids", columnDefinition ="integer []")
    private Integer[] refDbIds;

    @Type(type = "string-array")
    @Column(name="tag_ids", columnDefinition = "varchar(50) []")
    private String[] tagIds;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(
            name = "meta_table_tags_ref",
            joinColumns = {@JoinColumn(name = "table_id", referencedColumnName = "id", insertable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id", insertable = false, updatable = false)}
    )
    private Set<TagsEntity> tags;


    @Transient
    @JsonIgnore
    public String getFullName() {
        if (!StringUtil.isNullOrEmpty(schema)) {
            return schema + "." + name;
        } else {
            return name;
        }
    }
}

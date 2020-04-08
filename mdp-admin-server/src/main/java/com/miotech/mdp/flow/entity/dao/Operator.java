package com.miotech.mdp.flow.entity.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.miotech.mdp.common.model.LogicDeleteEntity;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.flow.constant.ParameterExecutionType;
import com.miotech.mdp.flow.constant.Platform;
import lombok.Data;
import org.hibernate.annotations.*;
import org.json.simple.JSONObject;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@Transactional
@SQLDelete(sql = "update operator set deleted = 1 where id = ?")
@Where(clause = "deleted = 0")
@Table(name = "operator", schema = "public")
public class Operator extends LogicDeleteEntity {

    @Column(name ="name" , columnDefinition = "varchar(50)")
    @JsonFormat
    private String name;

    @Column(name ="description", columnDefinition = "text")
    private String description;

    @Column(name = "platform", columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Type(type="jsonb")
    @Column(name="platformConfig", columnDefinition = "jsonb")
    @JsonRawValue
    private JSONObject platformConfig;

    @Column
    @JsonIgnore
    private Boolean allowPutback = false;

    @Column(name="parameterExecutionType", columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private ParameterExecutionType parameterExecutionType;

    @Column(name ="creator_id" , columnDefinition = "varchar(50)")
    private String creatorId;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "operator")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Parameter> parameters = new ArrayList<>();

    @Type(type = "string-array")
    @Column(name="tag_ids", columnDefinition = "varchar(50) []")
    private String[] tagIds;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "operator_tags_ref",
            joinColumns = {@JoinColumn(name = "operator_id", referencedColumnName = "id", insertable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id", insertable = false, updatable = false)}
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<TagsEntity> tagsEntities;

    @Transient
    public List<String> getParameterKeys() {
        return this.getParameters()
                .stream()
                .map(Parameter::getParameterKey)
                .distinct().collect(Collectors.toList());
    }
}

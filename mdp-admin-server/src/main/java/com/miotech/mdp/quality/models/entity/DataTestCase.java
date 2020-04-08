package com.miotech.mdp.quality.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miotech.mdp.common.model.BaseEntity;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.quality.models.enums.QualityPropertyEnum;
import com.miotech.mdp.quality.models.enums.RuleTypeEnum;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import com.miotech.mdp.quality.models.enums.TemplateTypeEnum;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2019/12/24 4:27 PM
 */

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "data_test_case", schema = "public")
@Data
@Accessors(chain = true)
@Proxy(lazy = false)
public class DataTestCase extends BaseEntity {


    @Column(columnDefinition = "text")
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "case_sql", columnDefinition = "text")
    private String caseSql;

    @Column(name = "validate_object", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private List<CaseValidator> validateObject;

    @Column(name = "db_id", columnDefinition = "int")
    private Integer dbId;

    @ManyToMany(cascade ={
            CascadeType.DETACH,
            CascadeType.REFRESH})
    @JoinTable(name = "data_test_case_tags",
                inverseJoinColumns = @JoinColumn(name = "tag_id", columnDefinition = "varchar(50)", referencedColumnName = "id", insertable = false, updatable = false),
    joinColumns = @JoinColumn(name = "case_id", columnDefinition = "varchar(50)", referencedColumnName = "id", insertable = false, updatable = false))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<TagsEntity> tags = new ArrayList<>();

    @ManyToMany(cascade = {
            CascadeType.DETACH,
            CascadeType.REFRESH})
    @JoinTable(name = "meta_table_test_ref",
            inverseJoinColumns = @JoinColumn(name = "table_id", columnDefinition = "varchar(50)",referencedColumnName = "id", insertable = false, updatable = false),
            joinColumns = @JoinColumn(name = "test_id", columnDefinition = "varchar(50)",referencedColumnName = "id", insertable = false, updatable = false))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<MetaTableEntity> relatedTables = new ArrayList<>();

    @JsonIgnore
    @Type(type = "string-array")
    @Column(name = "fields", columnDefinition = "varchar(100)[]")
    private String[] fields;

    @JsonIgnore
    @Type(type = "string-array")
    @Column(name = "tags_ids", columnDefinition = "varchar(50)[]")
    private String[] tagsIds;

    @Column(name = "is_block_flow", columnDefinition = "bool")
    private Boolean isBlockFlow;

    //==== table level=======
    @Column(name = "is_table_level", columnDefinition = "bool")
    private Boolean isTableLevel;

    @Column(name = "template_type", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private TemplateTypeEnum templateType;

    @Column(name = "template_operation", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private TemplateOperationEnum templateOperation;

    @Column(name = "owner_id", columnDefinition = "varchar(50)")
    private String ownerId;

    @Column(name = "rule_type", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private RuleTypeEnum ruleType;

    @Column(name = "quality_property", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private QualityPropertyEnum qualityProperty;

    @Column(name = "operation_field_ids", columnDefinition = "varchar(100)[]")
    @Type(type = "string-array")
    private String[] operatedFieldIds;
}

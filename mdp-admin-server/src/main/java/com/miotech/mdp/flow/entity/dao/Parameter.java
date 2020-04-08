package com.miotech.mdp.flow.entity.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miotech.mdp.common.model.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.transaction.Transactional;


@Entity
@Data
@Transactional
@Table(name = "config_parameters", schema = "public")
@JsonIgnoreProperties(value = {"createTime", "updateTime"})
public class Parameter extends BaseEntity {

    @Column(name = "parameter_type", columnDefinition = "varchar(255)")
    private String parameterType;

    @Column(name = "parameter_key", columnDefinition = "varchar(255)")
    private String parameterKey;

    @Column(name = "choice_url", columnDefinition = "varchar(255)")
    private String choiceUrl;

    @Type(type = "string-array")
    @Column(name = "choices", columnDefinition = "text []")
    private String[] choices;

    @Column(name = "default_value", columnDefinition = "text")
    private String defaultValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    @JsonIgnore
    private Operator operator;
}

package com.miotech.mdp.flow.entity.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.miotech.mdp.common.model.LogicDeleteEntity;
import com.miotech.mdp.flow.constant.ParameterExecutionType;
import com.miotech.mdp.flow.entity.bo.ArgumentInfo;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import lombok.Data;
import lombok.val;
import org.hibernate.annotations.*;
import org.json.simple.JSONObject;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Transactional
@SQLDelete(sql = "update flow_tasks set deleted = 1 where id = ?")
@Where(clause = "deleted = 0")
@Table(name = "flow_tasks", schema = "public")
public class FlowTask extends LogicDeleteEntity {

    @Column(name = "name", columnDefinition = "varchar(50)")
    private String name;

    @Type(type = "string-array")
    @Column(name="parent_task_ids", columnDefinition = "varchar(50) []")
    private String[] parentTaskIds = new String[]{};

    @Column(name="trigger_rule")
    private String triggerRule;

    @Column(name="retry_policy")
    private Integer retryPolicy = 0;

    @Column(name="task_order")
    private Integer taskOrder = 0;

    @Type(type = "jsonb")
    @Column(name="arguments", columnDefinition = "jsonb")
    private List<ArgumentInfo> arguments;

    @Type(type = "jsonb")
    @Column(name ="config" , columnDefinition = "jsonb")
    @JsonRawValue
    private JSONObject config = new JSONObject();

    @Column(name = "test_id", columnDefinition = "varchar(50)")
    private String testId;

    @Column(name ="creator_id" , columnDefinition = "varchar(50)")
    @JsonIgnore
    private String creatorId;

    @Column(name ="operator_id" , columnDefinition = "varchar(50)")
    @JsonFormat
    private String operatorId;

    @Column(name ="flow_id" , columnDefinition = "varchar(50)")
    @JsonFormat
    private String flowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id", insertable = false, updatable = false)
    @JsonIgnore
    private Flow flow;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    @JsonIgnore
    private Operator operator;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "flowTask")
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<TaskInstance> taskSet = new ArrayList<>();

    @ManyToMany(cascade = {
            CascadeType.DETACH,
            CascadeType.REFRESH})
    @JoinTable(name = "meta_table_flow_task_ref",
            inverseJoinColumns = @JoinColumn(name = "table_id", columnDefinition = "varchar(50)",referencedColumnName = "id", insertable = false, updatable = false),
            joinColumns = @JoinColumn(name = "flow_task_id", columnDefinition = "varchar(50)",referencedColumnName = "id", insertable = false, updatable = false))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<MetaTableEntity> relatedTables = new ArrayList<>();

    @Transient
    @JsonIgnore
    public List<String> getParams() {
        val parameterExecutionType = operator.getParameterExecutionType();

        List<String> params = new ArrayList<>();
        if (ParameterExecutionType.KEYNAME.equals(parameterExecutionType)) {
            JSONObject config = this.getConfig();
            config.keySet()
                    .forEach(x -> {
                        params.add(String.format("--%s", x));
                        params.add(config.get(x).toString());
                    });
        } else if (ParameterExecutionType.VALUES.equals(parameterExecutionType)) {
            JSONObject config = this.getConfig();
            config.keySet()
                    .forEach(x -> params.add(config.get(x).toString()));
        } else if (ParameterExecutionType.TASKID.equals(parameterExecutionType)) {
            params.add(this.getId());
        }
        return params;
    }
}

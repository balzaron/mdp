package com.miotech.mdp.table.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miotech.mdp.common.model.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Data
@Table(name = "meta_table_metrics")
public class MetaTableMetricsEntity extends BaseEntity {
    @Column(name = "table_id")
    private String tableId;

    @ManyToOne
    @JoinColumn(name = "table_id", insertable = false, updatable = false)
    @JsonIgnore
    private MetaTableEntity metaTable;

    @Type(type = "jsonb")
    @Column(name = "metric", columnDefinition = "jsonb")
    private Metric metric;

    @Transient
    @JsonIgnore
    public Integer getMetricCount() {
        return this.metric.getCount();
    }
}

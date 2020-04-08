package com.miotech.mdp.quality.models.entity;

import com.miotech.mdp.common.model.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author: shanyue.gao
 * @date: 2020/2/24 9:54 AM
 */
@Table(name = "case_coverage")
@Entity
@Data
@Accessors(chain = true)
public class CaseCoverageEntity extends BaseEntity {

    @Column(columnDefinition = "decimal", name = "covered_columns_num")
    private BigDecimal coveredColumnsNum;

    @Column(columnDefinition = "decimal", name = "total_columns_num")
    private BigDecimal totalColumnsNum;

    @Column(columnDefinition = "decimal", name = "coverage")
    private BigDecimal coverage;

    @Column(columnDefinition = "varchar(100)[]", name = "tag_ids")
    private String[] tagIds;
}

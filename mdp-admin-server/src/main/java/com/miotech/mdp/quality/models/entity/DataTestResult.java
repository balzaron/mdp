package com.miotech.mdp.quality.models.entity;

import com.miotech.mdp.common.model.BaseEntity;
import com.miotech.mdp.quality.models.enums.QualityPropertyEnum;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/1/6 10:25 AM
 */

@EqualsAndHashCode(callSuper = true)
@Table(name = "data_test_result", schema = "public", catalog = "mdp")
@Accessors(chain = true)
@Entity
@Data
@Proxy(lazy = false)
public class DataTestResult extends BaseEntity {

    @Column(columnDefinition = "boolean", name = "passed")
    private Boolean passed;

    @Type(type = "jsonb")
    @Column(name = "execute_result", columnDefinition = "jsonb")
    private List<ExecuteResult> executeResults;

    @Column(columnDefinition = "text", name = "error_catch_log")
    private String errorCatchLog;

    @Column(name = "impact_level", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private QualityPropertyEnum impactLevel;

    @Column(columnDefinition = "varchar(50)", name = "case_id")
    private String caseId;

    @Column(columnDefinition = "varchar(50)", name = "template_operation")
    @Enumerated(EnumType.STRING)
    private TemplateOperationEnum templateOperation;

}

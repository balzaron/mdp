package com.miotech.mdp.quality.repository;

import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.quality.models.entity.DataTestCase;
import com.miotech.mdp.quality.models.enums.QualityPropertyEnum;
import com.miotech.mdp.quality.models.enums.RuleTypeEnum;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author: shanyue.gao
 * @date: 2019/12/24 5:03 PM
 */
@Repository
public interface DataTestCaseRepository extends CrudRepository<DataTestCase, String>, JpaRepository<DataTestCase, String>, BaseRepository<DataTestCase> {

    /**
     *
     * @param name
     * @param description
     * @return
     */

    Page<DataTestCase> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    /**
     * @param tableId
     * @return
     */
    @Query(value = "SELECT tc FROM DataTestCase tc INNER JOIN tc.relatedTables tbl WHERE tbl.id = ?1")
    List<DataTestCase> findByRelatedTableId(String tableId);

    Page<DataTestCase> findAllByTemplateOperationAndRuleTypeAndQualityPropertyAndOwnerId(TemplateOperationEnum templateOperationEnum,
                                                                               RuleTypeEnum ruleTypeEnum,
                                                                               QualityPropertyEnum qualityPropertyEnum,
                                                                               String ownerId,
                                                                               Pageable pageable);

    Page<DataTestCase> findAllByTemplateOperationAndRuleType(TemplateOperationEnum templateOperationEnum,
                                                             RuleTypeEnum ruleTypeEnum,
                                                             Pageable pageable);


    Page<DataTestCase> findAllByTemplateOperation(TemplateOperationEnum templateOperationEnum, Pageable pageable);

    @Query(value = "SELECT * FROM data_test_case c inner join meta_table_test_ref m ON m.table_id = (:tableId) "
            + "where (:ownerId is null or c.owner_id = :ownerId) "
            + "and (:ruleType is null or c.rule_type = :ruleType) "
            + "and (:qualityProperty is null or c.quality_property = :qualityProperty) "
            + "and (:templateOperation is null or c.template_operation=:templateOperation)", nativeQuery = true)
    Page<DataTestCase> findAllByRelatedTables_IdAndOwnerIdAndRuleTypeAndQualityPropertyAndTemplateOperation(@Param("tableId") String tableId,
                                                                                                            @Param("ownerId") String ownerId,
                                                                                                            @Param("ruleType") String ruleType,
                                                                                                            @Param("qualityProperty") String qualityProperty,
                                                                                                            @Param("templateOperation") String templateOperation,
                                                                                                            Pageable pageable);

}

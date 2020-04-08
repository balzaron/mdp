package com.miotech.mdp.quality.repository;

import com.miotech.mdp.quality.models.entity.CaseCoverageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/2/24 10:27 AM
 */
public interface CaseCoverageRepository extends CrudRepository<CaseCoverageEntity, String>, JpaRepository<CaseCoverageEntity, String> {

    List<CaseCoverageEntity> findAllByCreateTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query(value = "select count(distinct f) from (select unnest(fields) as f from data_test_case) as dtcf", nativeQuery = true)
    Integer getCoveredColumnsNum();

    @Query(value = "select count(distinct mf.name)  " +
            "from meta_table_tags_ref as mttr " +
            "inner join meta_field mf " +
            "    on mttr.table_id = mf.table_id " +
            "where tag_id=?1", nativeQuery = true)
    Integer getFieldCountByTagName(String tagId);

    @Query(value = "select count(distinct f) " +
            "from (select unnest(fields) as f from data_test_case " +
            "      where id in( " +
            "          select distinct mttr2.test_id " +
            "          from meta_table_test_ref mttr2 " +
            "                   inner join meta_table_tags_ref as mttr " +
            "                              on mttr2.table_id=mttr.table_id " +
            "          where tag_id=?1 " +
            "      ) " +
            "     ) as tmp;", nativeQuery = true)
    Integer getCoveredColumnsNumberByRelatedTableTagName(String tagId);
}

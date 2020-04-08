package com.miotech.mdp.table.persistence;

import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends BaseRepository<MetaTableEntity>, JpaSpecificationExecutor<MetaTableEntity> {

    @Override
    @EntityGraph(value = "table.graph", type = EntityGraph.EntityGraphType.FETCH)
    Page<MetaTableEntity> findAll(Specification<MetaTableEntity> spec, Pageable pageable);

    @Query(value = "select * from public.meta_table where ref_db_ids @>  ARRAY[?1] and name = ?2", nativeQuery = true)
    List<MetaTableEntity> findByDbAndName(Integer dbId, String name);

    @Query(value = "select * from public.meta_table where ref_db_ids  @>  ARRAY[?1] and schema = ?2 and name = ?3", nativeQuery = true)
    List<MetaTableEntity> findByDbAndSchemaName(Integer dbId, String schema, String name);
}

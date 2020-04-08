package com.miotech.mdp.table.persistence;

import com.miotech.mdp.table.model.dao.MetaFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableColumnRepository extends JpaRepository<MetaFieldEntity, String> {
    @Query(value = "select * from public.meta_field where table_id = ?1", nativeQuery = true)
    List<MetaFieldEntity> findByTableId(String tableId);
}

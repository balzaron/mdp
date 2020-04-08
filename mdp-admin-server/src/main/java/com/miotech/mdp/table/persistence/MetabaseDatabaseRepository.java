package com.miotech.mdp.table.persistence;

import com.miotech.mdp.table.model.dao.MetabaseDatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetabaseDatabaseRepository extends JpaRepository<MetabaseDatabaseEntity, Integer> {

    @Query(value = "select distinct engine from public.metabase_database", nativeQuery = true)
    List<String> findDistinctDatabaseType();
}

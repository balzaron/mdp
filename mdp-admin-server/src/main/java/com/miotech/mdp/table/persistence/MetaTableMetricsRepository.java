package com.miotech.mdp.table.persistence;

import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.table.model.dao.MetaTableMetricsEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaTableMetricsRepository extends BaseRepository<MetaTableMetricsEntity> {
    List<MetaTableMetricsEntity> findAllByTableId(String tableId);

    MetaTableMetricsEntity findFirstByTableIdOrderByCreateTimeDesc(String tableId);
}

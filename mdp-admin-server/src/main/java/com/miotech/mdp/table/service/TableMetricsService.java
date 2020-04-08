package com.miotech.mdp.table.service;

import com.miotech.mdp.common.models.protobuf.metabase.QueryData;
import com.miotech.mdp.common.models.protobuf.metabase.Row;
import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.service.DatabaseGeneralQueryService;
import com.miotech.mdp.common.util.SQLQueryHelper;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.dao.MetaTableMetricsEntity;
import com.miotech.mdp.table.model.dao.Metric;
import com.miotech.mdp.table.persistence.MetaTableMetricsRepository;
import com.miotech.mdp.table.persistence.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableMetricsService extends BaseService<MetaTableMetricsEntity> {
    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private MetaTableMetricsRepository metaTableMetricsRepository;

    @Autowired
    DatabaseGeneralQueryService dbGeneralQueryService;

    public MetaTableMetricsEntity createMetric(String tableId, Metric metric) {
        MetaTableMetricsEntity tableMetrics = new MetaTableMetricsEntity();
        tableMetrics.setTableId(tableId);
        tableMetrics.setMetric(metric);
        return metaTableMetricsRepository.save(tableMetrics);
    }

    public MetaTableMetricsEntity getLatestMetrics(String tableId) {
        return metaTableMetricsRepository.findFirstByTableIdOrderByCreateTimeDesc(tableId);
    }

    /**
     * Returns number of total records in table
     * TODO: implement counter for NoSQL databases;
     * @param tableInfo
     * @return
     */
    public Integer getRecordsCount(MetaTableEntity tableInfo) {
        DBType dbType = DBType.valueOf(tableInfo.getDatabaseType().toUpperCase());
        switch (dbType) {
            case MYSQL:
            case POSTGRES:
            case ATHENA:
            case SQLSERVER:
            case HIVE:
            case SPARKSQL:
            case RDBMS:
                String sql = SQLQueryHelper.getCountTableRecordsSQL(tableInfo.getName(), tableInfo.getSchema(), dbType);
                QueryData queryData = dbGeneralQueryService.queryData(tableInfo.getDbId(), sql);
                if (queryData.getRowsCount() > 0) {
                    List<Row> rows = queryData.getRowsList();
                    return Integer.valueOf(rows.get(0).getValues(0).getNumberValue());
                } else {
                    throw new RuntimeException("Count table records returns empty result set.");
                }
            case MONGO:
                throw new RuntimeException("MongoDB not supported yet.");
            default:
                throw new RuntimeException("Database type not supported yet.");
        }
    }
}

package com.miotech.mdp.table.schema;

import com.miotech.sqllineage.SQLLineage;
import com.miotech.sqllineage.SQLLineageAnalyzer;
import com.miotech.sqllineage.model.DBType;
import com.miotech.sqllineage.model.MetaDataSet;
import org.springframework.stereotype.Component;
import scala.collection.Iterator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueryParser {

    private String dbMapping(String dbType) {
        switch (dbType.toUpperCase()) {
            case "PRESTO":
            case "ATHENA":
                return DBType.HIVE().toString();
            default:
                return DBType.withName(dbType.toUpperCase()).toString();
        }
    }

    public List<String> getSourceTablesNames(String dbType, String sql) {
        SQLLineage lineage = SQLLineageAnalyzer.parseLineage(sql, this.dbMapping(dbType));
        Iterator<MetaDataSet> itor = lineage.getSourceDataSets().iterator();
        List<MetaDataSet> dataSets = new ArrayList<>();
        while (itor.hasNext()) {
            dataSets.add(itor.next());
        }
        return dataSets.stream().map(MetaDataSet::aliasName)
                .collect(Collectors.toList());
    }
}

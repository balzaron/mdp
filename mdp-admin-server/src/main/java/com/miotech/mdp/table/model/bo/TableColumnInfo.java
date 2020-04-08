package com.miotech.mdp.table.model.bo;

import lombok.Data;
import lombok.NonNull;

@Data
public class TableColumnInfo {

    @NonNull
    private Integer currentDBId;

    @NonNull
    private String dbType;

    private String schema;

    @NonNull
    private String tableName;
}

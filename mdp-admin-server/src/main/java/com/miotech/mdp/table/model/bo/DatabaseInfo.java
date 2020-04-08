package com.miotech.mdp.table.model.bo;

import lombok.Data;

@Data
public class DatabaseInfo {

    private Integer id;

    private String dbType;

    private String schema;

    private String tableName;
}

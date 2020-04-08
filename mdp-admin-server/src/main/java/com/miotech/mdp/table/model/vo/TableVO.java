package com.miotech.mdp.table.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class TableVO {

    private String id;

    private String schema;

    private String name;

    private String lifecycle;

    private String description;

    private String dbType;

    private DatabaseVO currentDB;

    private List<DatabaseVO> referenceDBs;

    //第一期不允许手动改columns
    private List<TableColumnVO> tableColumns;

    private List<String> tags;

    private Integer testCount;
}

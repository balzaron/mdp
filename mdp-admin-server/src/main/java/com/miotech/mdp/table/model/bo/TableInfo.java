package com.miotech.mdp.table.model.bo;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
public class TableInfo {
    @NonNull
    private String dbType;
    @NonNull
    private String schema;
    @NonNull
    private String tableName;
    @NonNull
    private Integer currentDBId;
    @NonNull
    private List<Integer> referenceDBIds;

    private String description;
    @NonNull
    private String lifecycle;

    private List<String> tags;
}

package com.miotech.mdp.table.model.vo;

import lombok.Data;

@Data
public class TableColumnVO {
    private String id;

    private String name;

    private String type;

    private String description;

    private boolean key;

    private boolean unique;

    private boolean nullable;
}

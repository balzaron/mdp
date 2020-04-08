package com.miotech.mdp.table.model.bo;

import lombok.Data;

import java.util.List;

@Data
public class TableUpdate {

    private String lifecycle;

    private String description;

    private List<String> tags;
}

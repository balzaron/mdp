package com.miotech.mdp.table.model.bo;

import lombok.Data;

import java.util.List;

@Data
public class TableEdgeUpdate {

    private List<String> flowIds;

    private List<String> tags;
}

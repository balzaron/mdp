package com.miotech.mdp.table.model.bo;

import com.miotech.mdp.common.model.bo.PageInfo;
import lombok.Data;

import java.util.List;

@Data
public class TableSearchCondition extends PageInfo {

    private String tableName;

    private List<String> lifecycles;

    private List<String> dbTypes;

    private List<String> tags;
}

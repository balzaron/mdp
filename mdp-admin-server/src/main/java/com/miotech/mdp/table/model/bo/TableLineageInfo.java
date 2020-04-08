package com.miotech.mdp.table.model.bo;

import com.miotech.mdp.common.constant.GraphDirection;
import lombok.Data;

@Data
public class TableLineageInfo {

    private String tableId;

    private Integer layerNum;

    private GraphDirection direction = GraphDirection.BOTH;
}

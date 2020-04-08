package com.miotech.mdp.table.model.bo;

import com.miotech.mdp.common.model.bo.EdgeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class TableEdgeInfo extends EdgeInfo {

    private List<String> flowIds;

}

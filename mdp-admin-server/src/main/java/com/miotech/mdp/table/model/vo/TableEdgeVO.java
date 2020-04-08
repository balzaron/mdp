package com.miotech.mdp.table.model.vo;

import com.miotech.mdp.common.model.vo.EdgeVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TableEdgeVO extends EdgeVO {

    private List<FlowInfoVO> flows;
}

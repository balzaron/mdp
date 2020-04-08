package com.miotech.mdp.table.model.vo;

import com.miotech.mdp.common.model.vo.GraphVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TableLineageVO extends GraphVO<TableVertexVO, TableEdgeVO> {
}

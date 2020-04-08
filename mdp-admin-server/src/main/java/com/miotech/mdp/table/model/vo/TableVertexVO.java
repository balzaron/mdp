package com.miotech.mdp.table.model.vo;

import com.miotech.mdp.common.model.vo.VertexVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TableVertexVO extends VertexVO {

    private TableVO table;
}

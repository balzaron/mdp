package com.miotech.mdp.table.model.vo;

import com.miotech.mdp.common.model.vo.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TableSearchVO extends PageVO {

    private List<TableVO> tables;
}

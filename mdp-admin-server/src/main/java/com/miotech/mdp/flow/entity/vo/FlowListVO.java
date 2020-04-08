package com.miotech.mdp.flow.entity.vo;

import com.miotech.mdp.common.model.vo.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlowListVO extends PageVO {

    private List<FlowVO> flows;
}

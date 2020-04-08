package com.miotech.mdp.flow.entity.vo;

import com.miotech.mdp.common.model.vo.PageVO;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlowRunListVO extends PageVO {

    private List<FlowRun> runs;
}

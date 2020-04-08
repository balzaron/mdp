package com.miotech.mdp.flow.entity.vo;

import com.miotech.mdp.common.model.vo.PageVO;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskListVO extends PageVO {

    private List<TaskInstance> tasks;
}

package com.miotech.mdp.flow.entity.vo;

import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.util.Converter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class TaskStateCount {

    private String flowId;

    private String flowTaskId;

    private TaskState state;

    private Integer count;

    @Getter(AccessLevel.NONE)
    private String color;

    public String getColor() {
        return Converter.convertStateToColor(state.getName());
    }
}

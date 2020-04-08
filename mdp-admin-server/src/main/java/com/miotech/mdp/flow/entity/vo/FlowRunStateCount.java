package com.miotech.mdp.flow.entity.vo;

import com.miotech.mdp.flow.constant.FlowState;
import com.miotech.mdp.flow.util.Converter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class FlowRunStateCount {

    private FlowState state;

    private Integer count;

    @Getter(AccessLevel.NONE)
    private String color;

    public String getColor() {
       return Converter.convertStateToColor(state.getName());
    }
}

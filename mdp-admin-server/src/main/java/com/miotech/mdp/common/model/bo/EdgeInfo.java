package com.miotech.mdp.common.model.bo;

import lombok.Data;

import java.util.List;

@Data
public class EdgeInfo {

    private String inputId;

    private String outputId;

    private String inputModel;

    private String outputModel;

    private List<String> tags;
}

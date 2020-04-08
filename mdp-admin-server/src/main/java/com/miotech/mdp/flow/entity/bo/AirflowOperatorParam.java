package com.miotech.mdp.flow.entity.bo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AirflowOperatorParam {

    private String id;

    private String bash_command;

    private Integer retries;

    private String trigger_rule;

    private List<String> parent_ids;
}

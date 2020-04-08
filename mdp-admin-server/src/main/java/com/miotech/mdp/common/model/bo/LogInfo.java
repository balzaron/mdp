package com.miotech.mdp.common.model.bo;

import lombok.Data;

@Data
public class LogInfo {

    private String topic;

    private String userId;

    private String modelId;

    private String model;

    private String details;
}

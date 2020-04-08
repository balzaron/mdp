package com.miotech.mdp.common.client.spark;

import lombok.Data;

@Data
public class StatementRequest {
    private String code;

    private String kind = "spark";
}

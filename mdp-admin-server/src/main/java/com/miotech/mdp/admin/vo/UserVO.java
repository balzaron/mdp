package com.miotech.mdp.admin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserVO {

    @JsonProperty("username")
    private String username;

    private String id;
}

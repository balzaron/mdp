package com.miotech.mdp.common.model.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfo implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty(value = "username", required = true)
    private String username;

    @JsonProperty(value = "password", required = true)
    private String password;

}

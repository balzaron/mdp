package com.miotech.mdp.flow.entity.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.json.simple.JSONObject;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel
public class OperatorInfo {

    @ApiModelProperty(value = "Operator name", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "Operator description")
    private String description;

    @ApiModelProperty(value = "Method that parameters pass to operator when execution",
            required = true,
            allowableValues = "keyName,taskId,ignore")
    @NotBlank
    private String parameterExecutionType;

    @ApiModelProperty(value = "Supported operator execution platform",
            required = true,
            allowableValues = "bash,spark,docker")
    @NotBlank
    private String platform;

    @ApiModelProperty(value = "Platform configuration in json format, check the example. \n" +
            "bash: { \"command\": \"echo 1\"} \n" +
            "docker: {\"image\":\"ubuntu\",\"command\":\"echo 1\", \"name\": \"exmample\"} \n" +
            "spark: {\"jars\":\"example.jar\",\"files\":\"example.py\",\"application\":\"com.example.Application\",\"args\":\"1\"} \n" +
            "You can provide extra params as key value pair",
            example = "{ \"command\": \"echo 1\"}",
            required = true)
    private JSONObject platformConfig;

    @ApiModelProperty(value = "Parameters of operator")
    private List<ParameterInfo> parameters = new ArrayList<>();

    private String[] tags;

}

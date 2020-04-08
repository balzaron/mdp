package com.miotech.mdp.quality.models.vo.rule;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/13 2:21 PM
 */
@Data
@Accessors(chain = true)
public class GetTemplateBaseVo {

    @NotEmpty
    private List<String> fieldIds;

    @NotEmpty
    private Boolean isTableLevel;
}

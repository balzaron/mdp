package com.miotech.mdp.quality.models.vo.rule.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 4:41 PM
 */
@Data
@NoArgsConstructor
public class GetTemplateVo {

    @NotNull
    private List<String> fieldIds;

    @NotNull
    private Boolean isTableLevel;
}

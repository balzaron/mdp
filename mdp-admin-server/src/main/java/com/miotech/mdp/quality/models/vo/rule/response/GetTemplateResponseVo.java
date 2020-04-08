package com.miotech.mdp.quality.models.vo.rule.response;

import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 4:40 PM
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GetTemplateResponseVo {

    private List<TemplateOperationEnum> templateOperations;

}

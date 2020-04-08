package com.miotech.mdp.quality.models.vo.test;

import com.miotech.mdp.common.model.vo.PageVO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/1/7 6:53 PM
 */

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DataTestCasePageVo extends PageVO implements Serializable {

    private List<DataTestCaseResultVo> dataTestCases;
}

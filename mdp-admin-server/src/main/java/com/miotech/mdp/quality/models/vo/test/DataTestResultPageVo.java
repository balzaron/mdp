package com.miotech.mdp.quality.models.vo.test;

import com.miotech.mdp.common.model.vo.PageVO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/1/17 10:14 AM
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DataTestResultPageVo extends PageVO {

    private List<DataTestResultVo> dataTestResultVos;
}

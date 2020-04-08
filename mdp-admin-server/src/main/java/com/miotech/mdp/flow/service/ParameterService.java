package com.miotech.mdp.flow.service;

import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.entity.bo.ParameterInfo;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.dao.Parameter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

@Service
public class ParameterService extends BaseService<Parameter> {

    public Parameter saveOperatorParameter(Operator op, ParameterInfo parameterInfo) {
        Parameter p;
        if (StringUtil.isNullOrEmpty(parameterInfo.getId())) {
            p = new Parameter();
        } else {
            p = super.find(parameterInfo.getId());
        }
        setEntity(parameterInfo, p);
        p.setOperator(op);
        return p;
    }

    private void setEntity(ParameterInfo parameterInfo, Parameter parameter) {
        if (parameterInfo.getId() == null) {
            // should not update parameter key and type
            parameter.setParameterType(parameterInfo.getParameterType());
            parameter.setParameterKey(parameterInfo.getParameterKey());
        }


        String choiceUrl = parameterInfo.getChoiceUrl();
        String[] choices = parameterInfo.getChoices();
        if (!StringUtil.isNullOrEmpty(choiceUrl) && ArrayUtils.isNotEmpty(choices)) {
            throw new RuntimeException("Should not specify choices and choiceUrl");
        } else if (!StringUtil.isNullOrEmpty(choiceUrl)) {
            parameter.setChoiceUrl(choiceUrl);
        } else {
            parameter.setChoices(choices);
        }

        if (!StringUtil.isNullOrEmpty(parameterInfo.getDefaultValue())) {
            parameter.setDefaultValue(parameterInfo.getDefaultValue());
        }
    }
}

package com.miotech.mdp.flow.controller;

import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.entity.vo.FlowVO;
import com.miotech.mdp.flow.service.FlowService;
import com.miotech.mdp.flow.util.Converter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Api(tags = "metadata management")
public class TableToDataFlowController {

    @Autowired
    private FlowService flowService;

    @GetMapping("/table/{id}/flows")
    @ApiOperation("get table related flows")
    public Result<List<FlowVO>> getTableRelatedFlows(@PathVariable String id) {
        List<Flow> flows = flowService.getTableFlows(id);
        List<FlowVO> flowVOs = flows
                .stream()
                .map((Converter::convert2FlowVO))
                .collect(Collectors.toList());
        return Result.success(flowVOs);
    }
}

package com.miotech.mdp.flow.controller;

import com.miotech.mdp.common.log.AuditLog;
import com.miotech.mdp.common.log.EnableAuditLog;
import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.flow.entity.bo.OperatorSearch;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.bo.OperatorInfo;
import com.miotech.mdp.flow.entity.vo.OperatorListVO;
import com.miotech.mdp.flow.service.OperatorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Api(tags = "operator management")
@EnableAuditLog
public class OperatorController {

    @Autowired
    private OperatorService operatorService;

    @PostMapping("/operator/list")
    @ApiOperation("list operator")
    public Result<OperatorListVO> listOperators(@RequestBody OperatorSearch operatorSearch) {
        Page<Operator> operatorPage = operatorService.searchOperator(operatorSearch);
        OperatorListVO vo = new OperatorListVO();
        vo.setOperators(operatorPage.stream().collect(Collectors.toList()));
        vo.setPageNum(operatorPage.getNumber());
        vo.setPageSize(operatorPage.getSize());
        vo.setTotalCount(operatorPage.getTotalElements());
        return Result.success(vo);
    }

    @PostMapping("/operator")
    @ApiOperation("create operator")
    @AuditLog(topic = "create", model = "operator")
    public Result<Operator> createOperator(@RequestBody OperatorInfo operatorInfo) {
        return Result.success(operatorService.createOperator(operatorInfo));
    }

    @GetMapping("/operator/{id}")
    @ApiOperation("get operator")
    public Result<Operator> getOperator(@PathVariable String id) {
        return Result.success(operatorService.find(id));
    }

    @PutMapping("/operator/{id}")
    @ApiOperation("update operator")
    @AuditLog(topic = "update", model = "operator")
    public Result<Operator> updateOperator(@PathVariable String id,
                                           @RequestBody OperatorInfo operatorInfo) {
        return Result.success(operatorService.updateOperator(id, operatorInfo));
    }

    @DeleteMapping("/operator/{id}")
    @ApiOperation("delete operator")
    @AuditLog(topic = "delete", model = "operator")
    public Result<Object> deleteOperator(@PathVariable String id) {

        operatorService.delete(id);
        return Result.success();
    }
}

package com.miotech.mdp.quality.controller;

import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.quality.models.vo.test.DataTestCaseVo;
import com.miotech.mdp.quality.service.DataTestCaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
@Api(tags = "metadata management")
public class TableToDataTestCaseController {
    @Autowired
    private DataTestCaseService dataTestCaseService;

    @GetMapping("/table/{id}/tests")
    @ApiOperation("get table related tests")
    public Result<List<DataTestCaseVo>> getTableRelatedTests(@PathVariable String id) {
        List<DataTestCaseVo> vos = dataTestCaseService.getTableTestCases(id);
        return Result.success(vos);
    }
}

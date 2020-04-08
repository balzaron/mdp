package com.miotech.mdp.quality.controller;

import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.quality.models.entity.DataTestCase;
import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.exception.ResourceNotFoundException;
import com.miotech.mdp.quality.models.bo.DataTestCaseValidate;
import com.miotech.mdp.quality.models.vo.test.*;
import com.miotech.mdp.quality.service.DataTestCaseService;
import com.miotech.mdp.quality.service.DataTestResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


/**
 * @author: shanyue.gao
 * @date: 2019/12/24 5:08 PM
 */

@RestController
@RequestMapping("/api")
@Slf4j
@Api(tags = "data test management")
public class DataTestCaseController {

    @Autowired
    private DataTestCaseService dataTestCaseService;

    @Autowired
    private DataTestResultService dataTestResultService;

    @PostMapping("/data-test-case/retrieve")
    @ApiOperation("retrieve")
    public Result<DataTestCasePageVo> retrieve(@RequestBody SearchCaseRequestVo searchCaseRequestVo) {
        return Result.success(dataTestCaseService.retrieve(searchCaseRequestVo));
    }

    @GetMapping("/data-test-case/{id}")
    @ApiOperation("find by id")
    public Result<DataTestCaseResultVo> findById(@PathVariable String id) {
        DataTestCaseResultVo view = Optional.ofNullable(id)
                .map(it -> {
                    DataTestCaseResultVo testCase = dataTestCaseService.findById(id);
                    return testCase;
                }).orElseThrow(() -> new ResourceNotFoundException("no id found!"));
        return Result.success(view);
    }

    @PostMapping("/data-test-case")
    @ApiOperation(value = "add a case",
    notes = "### the validateObject is as below:\n" +
            "\t[ {'fieldName': 'str', 'operator': '==', 'expected': '0'} ]\n")
    public Result<DataTestCase> addCase(@RequestBody DataTestCaseRequestVo dataTestCaseVo) {
        return Result.success(dataTestCaseService.addCase(dataTestCaseVo));
    }

    @PutMapping("/data-test-case/{id}")
    @ApiOperation("update a case")
    public Result<String> updateCase(@PathVariable String id,
                                   @Valid @RequestBody DataTestCaseRequestVo dataTestCaseVo) {
        return Result.success(dataTestCaseService.updateCase(id, dataTestCaseVo));
    }

    @DeleteMapping("/data-test-case/{id}")
    @ApiOperation("delete a case")
    public Result<String> deleteCase(@PathVariable String id) {
        return Result.success(dataTestCaseService.deleteCase(id));
    }

    @PostMapping("/data-test-case/batch-delete")
    @ApiOperation("batch delete cases")
    public Result<String> deleteCases(@RequestBody List<String> ids) {
        return Result.success(dataTestCaseService.deleteCases(ids));
    }

    @PostMapping("/data-test-case/validate")
    @ApiOperation("validate a case")
    public Result<DataTestCaseValidationResult> validateCase(@RequestBody DataTestCaseValidate testcaseValidateVo) {

        return Result.success(dataTestCaseService.validateCase(testcaseValidateVo));
    }

    @GetMapping("/data-test-result/list")
    public Result<Page<DataTestResult>> findAll(@RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
                                                @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                @RequestParam(value = "sortField", defaultValue = "id") String sortField) {
        return Result.success(dataTestResultService.findAll(pageNum, pageSize, sortField));
    }

    @PostMapping("/data-test-result/find-by-case-id")
    public Result<DataTestResultPageVo> findByCaseId(@RequestBody DataTestResultFindByCaseIdRequestVo requestVo) {
        Integer pageNum = requestVo.getPageNum();
        Integer pageSize = requestVo.getPageSize();
        String caseId = requestVo.getCaseId();
        return Result.success(dataTestResultService.findByCaseId(caseId, pageNum, pageSize));
    }

    @GetMapping("/data-test-result/{id}")
    public Result<DataTestResult> findResultById(@PathVariable String id) {
        return Result.success(dataTestResultService.findById(id));
    }

//    @DeleteMapping("/data-test-case/delete-all")
//    public Result<String> deleteAll() {
//        dataTestCaseService.deleteAll();
//        return Result.success("success!");
//    }

}

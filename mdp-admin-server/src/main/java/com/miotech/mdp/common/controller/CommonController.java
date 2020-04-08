package com.miotech.mdp.common.controller;

import com.miotech.mdp.common.constant.Lifecycle;
import com.miotech.mdp.common.model.bo.TagSearchCondition;
import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.common.model.vo.TagVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api")
public class CommonController {

    @ApiOperation("get lifecycle list")
    @GetMapping("/lifecycle")
    public Result<List<String>> lifecycle() {
        return Result.success(Arrays.asList(Lifecycle.values())
                .stream().map(Lifecycle::getName)
                .collect(Collectors.toList()));
    }

    @ApiOperation("search tag")
    @PostMapping("/tag/search")
    public Result<TagVO> tagSearch(@RequestBody TagSearchCondition tagSearchCondition) {
        return Result.success();
    }
}

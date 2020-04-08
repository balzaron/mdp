package com.miotech.mdp.quality.controller;

import com.miotech.mdp.quality.models.entity.CaseCoverageEntity;
import com.miotech.mdp.quality.models.vo.test.CaseCoverageResultVo;
import com.miotech.mdp.quality.service.CaseCoverageService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.miotech.mdp.quality.constant.Constant.DATE_TIME_FORMATTER;

/**
 * @author: shanyue.gao
 * @date: 2020/2/24 11:18 AM
 */

@RestController
@RequestMapping("/api")
@Slf4j
@Api(tags = "data test management")
public class CaseCoverageController {

    @Autowired
    private CaseCoverageService caseCoverageService;

    @GetMapping("/data-test-coverage/calculate")
    public CaseCoverageResultVo calculate(@RequestParam String tagName) {
        CaseCoverageEntity entity = caseCoverageService.calculateCoverage(tagName);
        return new CaseCoverageResultVo().setCoverage(entity.getCoverage())
                .setCreateTime(entity.getCreateTime())
                .setNumerator(entity.getCoveredColumnsNum())
                .setDenominator(entity.getTotalColumnsNum());
    }

    @GetMapping("/data-test-coverage/get-by-time-range")
    public List<CaseCoverageResultVo> getByRange(@RequestParam(required = false) String start, @RequestParam(required = false) String end) {
        LocalDateTime startDate = Optional.ofNullable(start)
                .map(d -> LocalDateTime.parse(d, DATE_TIME_FORMATTER))
                .orElse(LocalDateTime.now().plusDays(-1));
        LocalDateTime endDate = Optional.ofNullable(end).map(
                d -> LocalDateTime.parse(d, DATE_TIME_FORMATTER)
        ).orElse(LocalDateTime.now());
        List<CaseCoverageEntity> target = caseCoverageService.getCoverageByDate(startDate, endDate);
        return target.stream().map(
                e -> new CaseCoverageResultVo().setDenominator(e.getTotalColumnsNum())
                .setNumerator(e.getCoveredColumnsNum())
                .setCreateTime(e.getCreateTime())
                .setCoverage(e.getCoverage())
        ).collect(Collectors.toList());
    }
}

package com.miotech.mdp.quality.service.impl;

import cn.hutool.core.util.StrUtil;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.service.TagService;
import com.miotech.mdp.quality.exception.DividedZeroException;
import com.miotech.mdp.quality.models.entity.CaseCoverageEntity;
import com.miotech.mdp.quality.repository.CaseCoverageRepository;
import com.miotech.mdp.quality.service.CaseCoverageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/2/24 10:04 AM
 */
@Service
public class CaseCoverageServiceImpl implements CaseCoverageService {

    @Autowired
    private CaseCoverageRepository caseCoverageRepository;

    @Autowired
    private TagService tagService;
    private static final int ROUND = 20;
    @Override
    public CaseCoverageEntity calculateCoverage(String tagName) {
        TagsEntity tag = tagService.findTags(Collections.singletonList(tagName)).get(0);
        String tagId = tag.getId();

        Integer coveredColumnsCount =caseCoverageRepository.getCoveredColumnsNumberByRelatedTableTagName(tagId);
        Integer totalColumnsCount = caseCoverageRepository.getFieldCountByTagName(tagId);
        String[] tagIds = {tagId};

        if (totalColumnsCount.equals(0)){
            throw new DividedZeroException(StrUtil.format("The number of table with tag {} is 0", tagName));
        }
        // calculate all tables fields number;

        BigDecimal covered = BigDecimal.valueOf(coveredColumnsCount);
        BigDecimal total = BigDecimal.valueOf(totalColumnsCount);

        BigDecimal coverage = covered.divide(total, ROUND, BigDecimal.ROUND_FLOOR);

        return caseCoverageRepository.saveAndFlush(
                new CaseCoverageEntity().setTotalColumnsNum(total)
                .setCoveredColumnsNum(covered)
                .setCoverage(coverage)
                .setTagIds(tagIds)
        );
    }

    @Override
    public List<CaseCoverageEntity> getCoverageByDate(LocalDateTime start, LocalDateTime end) {
        return caseCoverageRepository.findAllByCreateTimeBetween(start, end);
    }


}

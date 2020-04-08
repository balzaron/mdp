package com.miotech.mdp.quality.service;

import com.miotech.mdp.quality.models.entity.CaseCoverageEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/2/24 9:50 AM
 */
public interface CaseCoverageService {

    CaseCoverageEntity calculateCoverage(String tagName);

    List<CaseCoverageEntity> getCoverageByDate(LocalDateTime start, LocalDateTime end);

}

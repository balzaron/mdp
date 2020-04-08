package com.miotech.mdp.quality.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.persistent.TagsRepository;
import com.miotech.mdp.table.persistence.TableRepository;
import com.miotech.mdp.quality.models.entity.DataTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author: shanyue.gao
 * @date: 2020/1/9 4:13 PM
 */

@Component
public class MergeDataTestCaseRepository {

    @Autowired
    private DataTestCaseRepository dataTestCaseRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private TableRepository tableRepository;

    @Transactional
    public DataTestCase save(DataTestCase dataTestCase) {
        if (!CollectionUtil.isEmpty(dataTestCase.getTags())) {
            dataTestCase.getTags().forEach(tagsEntity -> {
                if (!StrUtil.isEmpty(tagsEntity.getName())) {
                    Optional<TagsEntity> tagsEntityOptional = tagsRepository.findOne(Example.of(tagsEntity));
                    tagsEntity.setId(tagsEntityOptional.orElseGet(
                            () -> tagsRepository.saveAndFlush(tagsEntity)).getId()
                    );
                }
            });
        }

        if (!CollectionUtil.isEmpty(dataTestCase.getRelatedTables())) {
            dataTestCase.getRelatedTables().forEach(
                    r -> {
                        if (!StrUtil.isEmpty(r.getName())) {
                            r.setId(tableRepository.findOne(Example.of(r)).orElseGet(
                                    () -> tableRepository.saveAndFlush(r)).getId());
                        }
                    }
            );
        }

        return dataTestCaseRepository.saveAndFlush(dataTestCase);
    }
}

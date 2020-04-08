package com.miotech.mdp.table.persistence;

import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.persistent.TagsRepository;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

@Repository
@Slf4j
public class EnhancedTableRepository {

    @Autowired
    TableRepository tableRepository;

    @Autowired
    TagsRepository tagsRepository;

    @Transactional(rollbackFor = Exception.class)
    public MetaTableEntity save(MetaTableEntity metaTableEntity) {
        if (!CollectionUtils.isEmpty(metaTableEntity.getTags())) {
            metaTableEntity.getTags().forEach(tagsEntity -> {
                if (!StringUtils.isEmpty(tagsEntity.getName())) {
                    Optional<TagsEntity> tagsEntityOptional = tagsRepository.findOne(Example.of(tagsEntity));
                    tagsEntity.setId(tagsEntityOptional.orElseGet(() -> tagsRepository.saveAndFlush(tagsEntity)).getId());
                }
            });
        }

        return tableRepository.saveAndFlush(metaTableEntity);
    }
}

package com.miotech.mdp.quality.util;

import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.persistent.TagsRepository;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.persistence.TableRepository;
import com.miotech.mdp.quality.exception.ResourceNotFoundException;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/1/12 12:03 PM
 */

@Component
public class AliasUtil {

    @Autowired
    private TagsRepository tagsRepository;

    @Transactional
    public List<TagsEntity> alias2Tag(List<TagsEntity> tags) {
        List<TagsEntity> tagsEntities = new ArrayList<>();
        tags.forEach(
                tag -> tagsEntities.add(tagsRepository.findOne(Example.of(tag))
                        .orElseGet(
                                () -> {
                                    TagsEntity tagsEntity = new TagsEntity();
                                    tagsEntity.setName(tag.getName());
                                    tagsEntity.setColor(tag.getColor());
                                    return tagsRepository.saveAndFlush(tagsEntity);
                                })
                )
        );
        return tagsEntities;
    }

    @Autowired
    private TableRepository tableRepository;

    @Transactional
    public List<MetaTableEntity> alias2Table(List<RelatedTableVO> tables) {
        List<MetaTableEntity> metaTableEntities = new ArrayList<>();
        if (tables == null) {
            return metaTableEntities;
        }
        tables.forEach(
                table -> metaTableEntities.add(tableRepository.findById(table.getId())
                .orElseThrow(() -> new ResourceNotFoundException("no id: "+ table.getId() +"found!"))
                ));
        return metaTableEntities;
    }
}

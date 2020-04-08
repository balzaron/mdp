package com.miotech.mdp.common.service;

import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.persistent.TagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagsRepository tagsRepository;

    public List<TagsEntity> findTags(List<String> tagNames) {
        return tagNames.stream()
                .map(this::findOrCreateTag)
                .collect(Collectors.toList());
    }

    public TagsEntity findOrCreateTag(String name) {
        TagsEntity tagsEntity = new TagsEntity();
        tagsEntity.setName(name);
        Optional<TagsEntity> tagsEntityOptional = tagsRepository.findOne(Example.of(tagsEntity));
        return tagsEntityOptional
                .orElseGet(() -> tagsRepository.save(tagsEntity));
    }
}

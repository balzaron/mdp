package com.miotech.mdp.common.model;

import com.miotech.mdp.common.model.bo.TagInfo;
import com.miotech.mdp.common.model.dao.TagsEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ModelConverter {

    public static TagsEntity convertToTagsEntity(TagInfo tagInfo) {
        TagsEntity entity = new TagsEntity();
        entity.setId(tagInfo.getId());
        entity.setName(tagInfo.getName());
        entity.setColor(tagInfo.getColor());
        return entity;
    }

    public static List<TagsEntity> convertToTagsEntitys(List<String> tagNames) {
        return tagNames.stream().map(ModelConverter::convertToTagsEntity).collect(Collectors.toList());
    }


    public static TagsEntity convertToTagsEntity(String tagName) {
        TagInfo tagInfo = new TagInfo();
        tagInfo.setName(tagName);
        return convertToTagsEntity(tagInfo);
    }

}

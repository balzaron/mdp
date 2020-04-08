package com.miotech.mdp.quality.util;

import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.model.vo.TagVO;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.quality.models.entity.DataTestCase;
import com.miotech.mdp.quality.models.vo.test.DataTestCaseVo;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: shanyue.gao
 * @date: 2020/1/9 11:28 AM
 */
public class Converter {

    public static List<MetaTableEntity> convert2MetaTableEntities(List<RelatedTableVO> relatedTableVOS) {
        return relatedTableVOS.stream()
                .map(v -> {
                    MetaTableEntity tableEntity = new MetaTableEntity();
                    tableEntity.setDatabaseType(v.getDbType());
                    tableEntity.setLifecycle(v.getLifecycle());
                    tableEntity.setName(v.getName());
                    tableEntity.setSchema(v.getSchema());
//                    tableEntity.setId(v.getId());
                    return tableEntity;
                })
                .collect(Collectors.toList());

    }

    public static List<TagVO> convert2TagVo(List<TagsEntity> tagsEntities) {
        return tagsEntities.stream()
                .map(t -> {
                    TagVO tagVO = new TagVO();
                    tagVO.setName(t.getName());
//                    tagVO.setId(t.getId());
                    tagVO.setColor(t.getColor());
                    return tagVO;
                }
                )
                .collect(Collectors.toList());
    }

    public static List<RelatedTableVO> convert2RelatedTableVo(List<MetaTableEntity> metaTableEntities) {
        return metaTableEntities.stream()
                .map(m -> {
                    RelatedTableVO relatedTableVO = new RelatedTableVO();
                    relatedTableVO.setDbType(m.getDatabaseType());
                    relatedTableVO.setId(m.getId());
                    relatedTableVO.setSchema(m.getSchema());
                    relatedTableVO.setName(m.getName());
                    relatedTableVO.setLifecycle(m.getLifecycle());
                    return relatedTableVO;
                }).collect(Collectors.toList());
    }

    public static List<TagsEntity> convert2TagsEntities(List<TagVO> tagVOS) {
        return tagVOS.stream()
                .map(t -> {
                    TagsEntity tagsEntity = new TagsEntity();
                    tagsEntity.setColor(t.getColor());
                    tagsEntity.setName(t.getName());
//                    tagsEntity.setId(t.getId());
                    return tagsEntity;
                }).collect(Collectors.toList());
    }

    public static List<DataTestCaseVo> convert2DataTestCaseVo(List<DataTestCase> testCases) {
        PropertyMap<DataTestCase, DataTestCaseVo> mapper = new PropertyMap<DataTestCase, DataTestCaseVo>() {
            @Override
            protected void configure() {
                // ModelMapper cannot convert `alibaba.fastjson.JSONArray` Object, skip it
                skip(destination.getValidateObject());
            }
        };
        return testCases
                .stream()
                .map(dataTestCase -> {
                    ModelMapper modelMapper = new ModelMapper();
                    modelMapper
                            .createTypeMap(dataTestCase, DataTestCaseVo.class)
                            .addMappings(mapper);
                    DataTestCaseVo vo = modelMapper.map(dataTestCase, DataTestCaseVo.class);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
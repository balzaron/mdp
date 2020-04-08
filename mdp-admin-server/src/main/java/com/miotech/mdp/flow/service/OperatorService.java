package com.miotech.mdp.flow.service;

import com.miotech.mdp.common.jpa.CustomFilter;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.service.TagService;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.constant.ParameterExecutionType;
import com.miotech.mdp.flow.constant.Platform;
import com.miotech.mdp.flow.entity.bo.OperatorInfo;
import com.miotech.mdp.flow.entity.bo.OperatorSearch;
import com.miotech.mdp.flow.entity.bo.ParameterInfo;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.dao.Parameter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class OperatorService extends BaseService<Operator> {

    @Autowired
    ParameterService parameterService;

    @Autowired
    TagService tagService;

    public Page<Operator> searchOperator(OperatorSearch operatorSearch) {

        Specification<Operator> specification = (Specification<Operator>) (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (!StringUtil.isNullOrEmpty(operatorSearch.getName())) {
                predicates.add(cb.like(
                        cb.upper(root.get("name")),
                        "%" + operatorSearch.getName().toUpperCase() + "%"));
            }

            if (ArrayUtils.isNotEmpty(operatorSearch.getTags())) {
                val tagIds = tagService.findTags(Arrays.asList(operatorSearch.getTags()))
                        .stream().map(TagsEntity::getId).collect(Collectors.toList());
                val customFilter = new CustomFilter<Operator>();
                predicates.add(cb.isTrue(
                        customFilter
                                .udfArrayContains(cb, root.get("tagIds"), tagIds))
                );
            }
            if (predicates.isEmpty()) {
                return cb.isNotNull(root.get("id"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageableRequest = PageRequest.of(operatorSearch.getPageNum(),
                operatorSearch.getPageSize(),
                Sort.by("createTime").descending());

        return this.page(specification, pageableRequest);
    }

    public Operator createOperator(OperatorInfo operatorInfo) {
        validateOperator(operatorInfo);
        Operator operatorEntity = new Operator();
        setEntity(operatorEntity, operatorInfo);
        if (getCurrentUser() != null) {
            operatorEntity.setCreatorId(getCurrentUser().getId());
        }
        return super.save(operatorEntity);
    }

    public Operator updateOperator(String operatorId, OperatorInfo operatorInfo) {
        validateOperator(operatorInfo);
        Operator operatorEntity = super.find(operatorId);
        setEntity(operatorEntity, operatorInfo);
        return super.save(operatorEntity);
    }

    private void setEntity(Operator operatorEntity, OperatorInfo operatorInfo) {
        if (!StringUtil.isNullOrEmpty(operatorInfo.getName())) {
            operatorEntity.setName(operatorInfo.getName());
        }

        if (operatorInfo.getDescription() != null) {
            operatorEntity.setDescription(operatorInfo.getDescription());
        }

        if (operatorInfo.getParameterExecutionType() != null) {
            operatorEntity.setParameterExecutionType(
                    ParameterExecutionType.fromName(operatorInfo.getParameterExecutionType())
            );
        }

        // did not allow change platform, only platformconfig
        if (operatorEntity.getId() == null) {
            operatorEntity.setPlatform(Platform.fromName(
                    operatorInfo.getPlatform())
            );
        }

        if (operatorInfo.getPlatformConfig() != null) {
            operatorEntity.setPlatformConfig(operatorInfo.getPlatformConfig());
        }

        if (operatorInfo.getParameters()!=null) {
            List<Parameter> parameterList = operatorInfo.getParameters()
                    .stream()
                    .map(x -> parameterService.saveOperatorParameter(operatorEntity, x))
                    .collect(Collectors.toList());
            operatorEntity.setParameters(parameterList);
        }

        if (operatorInfo.getTags()!= null) {
            List<TagsEntity> tagsEntities = tagService.findTags(Arrays.asList(operatorInfo.getTags()));
            String[] tagIds = tagsEntities.stream()
                    .map(TagsEntity::getId)
                    .toArray(String[]::new);
            operatorEntity.setTagIds(tagIds);
            operatorEntity.setTagsEntities(tagsEntities);
        }
    }

    private void validateOperator(OperatorInfo operatorInfo) {
        Platform platform = Platform.fromName(operatorInfo.getPlatform());
        if (platform == null) {
            throw  new RuntimeException("Invalid Platform type. ");
        }
        switch (platform) {
            case SPARK:
                requiredKeys(operatorInfo.getPlatformConfig(), new String[] { "jars", "application", "args", "files" });
                break;
            case DOCKER:
                requiredKeys(operatorInfo.getPlatformConfig(), new String[] { "image", "command", "name" });
                break;
            case BASH:
                requiredKeys(operatorInfo.getPlatformConfig(), new String[] { "command" });
                break;
            case BUILTIN:
                requiredKeys(operatorInfo.getPlatformConfig(), new String[] { "className" });
                break;
            default:
                throw  new RuntimeException(String.format("Platform not supported: \"%s\" ", operatorInfo.getPlatform()));
        }

        if (ParameterExecutionType.fromName(operatorInfo.getParameterExecutionType()) == null) {
            throw  new RuntimeException("Invalid Platform Execution type: " + operatorInfo.getParameterExecutionType());
        }

        if (StringUtil.isDuplicate(operatorInfo.getParameters()
                .stream()
                .map(ParameterInfo::getParameterKey)
                .collect(Collectors.toList()))){
            throw  new RuntimeException("Duplicate in parameterKey");
        }
    }

    private boolean requiredKeys(JSONObject config, String[] keys) {
        boolean requiredAll = true;
        for (String x : keys) {
            if (config.get(x) == null) {
                requiredAll = false;
            }
        }
        if (!requiredAll) {
            String expectedKeys = String.join(",", keys);
            throw  new RuntimeException("Invalid Platform Config. Expected keys: " + expectedKeys);
        }
        return true;
    }
}

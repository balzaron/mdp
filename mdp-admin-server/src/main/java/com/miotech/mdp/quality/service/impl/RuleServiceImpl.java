package com.miotech.mdp.quality.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.persistent.UsersRepository;
import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.service.TagService;
import com.miotech.mdp.common.util.SQLQueryHelper;
import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.entity.DataTestCase;
import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.models.enums.*;
import com.miotech.mdp.quality.models.vo.rule.FilterVo;
import com.miotech.mdp.quality.models.vo.rule.SortField;
import com.miotech.mdp.quality.models.vo.rule.request.CreateCustomizedRuleVo;
import com.miotech.mdp.quality.models.vo.rule.request.CreatePresetRuleTemplateVo;
import com.miotech.mdp.quality.models.vo.rule.request.FetchRulesListVo;
import com.miotech.mdp.quality.models.vo.rule.request.GetTemplateVo;
import com.miotech.mdp.quality.models.vo.rule.response.*;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import com.miotech.mdp.quality.repository.DataTestCaseRepository;
import com.miotech.mdp.quality.service.RuleService;
import com.miotech.mdp.quality.util.ExecutorUtil;
import com.miotech.mdp.quality.util.SqlGenerator;
import com.miotech.mdp.table.model.dao.MetaFieldEntity;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.vo.TableVO;
import com.miotech.mdp.table.persistence.TableColumnRepository;
import com.miotech.mdp.table.persistence.TableRepository;
import com.miotech.mdp.table.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Name;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotEmpty;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.miotech.mdp.quality.constant.Constant.VALIDATE_RESULT;

/**
 * @author: shanyue.gao
 * @date: 2020/3/11 5:57 PM
 */
@Service
@Slf4j
public class RuleServiceImpl extends BaseService<DataTestCase> implements RuleService {

    @Autowired
    private DataTestCaseRepository dataTestCaseRepository;

    @Autowired
    private TableColumnRepository columnRepository;

    private static final String[] NUMERIC_TYPE = {"bigint", "double", "float", "decimal"};

    private static final String[] STRING_TYPE = {"varchar", "text", "char", "type/text"};

    @Autowired
    private ExecutorUtil executorUtil;

    @Autowired
    private TableService tableService;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TagService tagService;

    @Override
    public GetTemplateResponseVo getTemplate(GetTemplateVo getTemplate) {
        List<String> fieldIds = getTemplate.getFieldIds();
        Boolean isTableLevel = getTemplate.getIsTableLevel();

        if (isTableLevel) {
            return new GetTemplateResponseVo().setTemplateOperations(TemplateOperationEnum.list(TemplateTypeEnum.TABLE));
        }

        if (fieldIds.size() > 1) {
            return new GetTemplateResponseVo().setTemplateOperations(TemplateOperationEnum.list(TemplateTypeEnum.STRING));
        } else {
           String id = fieldIds.get(0);
           String type = columnRepository.findById(id).map(
                   MetaFieldEntity::getDatabaseType
           ).orElse("unknown");

           if (ArrayUtil.contains(NUMERIC_TYPE, type.toLowerCase())) {
               return new GetTemplateResponseVo().setTemplateOperations(TemplateOperationEnum.list(TemplateTypeEnum.NUMERIC));
           } else if (ArrayUtil.contains(STRING_TYPE, type.toLowerCase() )) {
               return new GetTemplateResponseVo().setTemplateOperations(TemplateOperationEnum.list(TemplateTypeEnum.STRING));
           }else {
               return new GetTemplateResponseVo().setTemplateOperations(TemplateOperationEnum.list(TemplateTypeEnum.STRING));
           }
        }

    }

    @Override
    public CreateCustomizedRuleResponseVo createCustomizedRule(CreateCustomizedRuleVo createCustomizedRule) {
        ModelMapper modelMapper = new ModelMapper();
        DataTestCase dataTestCase = modelMapper.map(createCustomizedRule, DataTestCase.class);
        dataTestCase.setQualityProperty(QualityPropertyEnum.ACCURACY);
        List<String> tags = createCustomizedRule.getTags();
        String tableId = createCustomizedRule.getRelatedTables().get(0).getId();
        dataTestCase.setRuleType(RuleTypeEnum.CUSTOMIZED_SQL);
        dataTestCase.setRelatedTables(Collections.singletonList(tableService.find(tableId)));
        return modelMapper.map(dataTestCaseRepository.saveAndFlush(setTags(tags, dataTestCase)), CreateCustomizedRuleResponseVo.class);
    }

    @Override
    public CreatePresetRuleTemplateResponseVo createPresetRuleTemplate(CreatePresetRuleTemplateVo createPresetRuleTemplate) {


        ModelMapper modelMapper = new ModelMapper();
        DataTestCase dataTestCase = modelMapper.map(createPresetRuleTemplate, DataTestCase.class);
        List<RelatedTableVO> tables = createPresetRuleTemplate.getRelatedTables();
        String tableId = tables.get(0).getId();
        Integer dbId = tableService.find(tableId).getDbId();

        TemplateOperationEnum operation = createPresetRuleTemplate.getTemplateOperation();

        TableVO table = tableService.getTable(tableId);
        String tableName = table.getName();

        List<String> fieldIds = createPresetRuleTemplate.getFieldIds();
        List<String> fieldNames = Optional.ofNullable(fieldIds)
                .map(f -> f.stream().map(i-> columnRepository.findById(i).get().getName()).collect(Collectors.toList())).orElse(null);
        List<CaseValidator> validators = createPresetRuleTemplate.getValidateObject();
        List<CaseValidator> caseValidators = validators.stream().map(v-> v.setFieldName(VALIDATE_RESULT)).collect(Collectors.toList());
        List<String> tags = createPresetRuleTemplate.getTags();

        List<String> fields = createPresetRuleTemplate.getFieldIds()
                .stream().map(i->columnRepository.getOne(i).getName()).collect(Collectors.toList());

        Name tableQuery = SQLQueryHelper.getQualifiedTableName(tableName, table.getSchema().toUpperCase(), DBType.valueOf(table.getDbType())).unquotedName();
        tableName = tableQuery.toString();
        String caseSql = SqlGenerator.generateSqlByOperation(operation,
                createPresetRuleTemplate.getIsTableLevel(),
                fields,
                tableName);

        String[] operatedFieldIds = Convert.toStrArray(createPresetRuleTemplate.getFieldIds());


        dataTestCase.setCaseSql(caseSql)
                .setIsBlockFlow(createPresetRuleTemplate.getIsBlockFlow())
                .setIsTableLevel(createPresetRuleTemplate.getIsTableLevel())
                .setDbId(dbId)
                .setQualityProperty(operation.getQualityProperty())
                .setTemplateType(operation.getTemplateType())
                .setTemplateOperation(createPresetRuleTemplate.getTemplateOperation())
                .setFields(Convert.toStrArray(fieldNames))
                .setValidateObject(caseValidators)
                .setRuleType(RuleTypeEnum.PRESET_TEMPLATE)
                .setRelatedTables(Collections.singletonList(tableService.find(tableId)))
                .setOperatedFieldIds(operatedFieldIds);

        return modelMapper.map(dataTestCaseRepository.saveAndFlush(setTags(tags, dataTestCase)), CreatePresetRuleTemplateResponseVo.class);
    }

    @Override
    public FetchRulesListResponsePageVo fetchRulesList(FetchRulesListVo fetchRulesList) {

        Integer pageNum = fetchRulesList.getPageNum();
        Integer pageSize = fetchRulesList.getPageSize();
        SortField sortField = fetchRulesList.getSortField();
        FilterVo filterVo = fetchRulesList.getFilters();
        String tableId = fetchRulesList.getTableId();

        Pageable pageable;
        if (ObjectUtil.isNotNull(sortField)  && ObjectUtil.isNotNull(sortField.getField()) && ObjectUtil.isNotNull(sortField.getOrder())) {
            String sortFieldString = sortField.getField();
            OrderEnum order = sortField.getOrder();
            Sort.Direction sort = order.equals(OrderEnum.ascend) ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(sort, sortFieldString));
        } else {
            pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        }
        ModelMapper modelMapper = new ModelMapper();

        String ownerId = Optional.ofNullable(filterVo.getOwnerId())
                .orElse(null);
        String ruleType = Optional.ofNullable(filterVo.getRuleType())
                .map(Enum::name).orElse(null);

        String qualityProperty = Optional.ofNullable(filterVo.getQualityProperty())
                .map(Enum::name).orElse(null);
        String templateOperation = Optional.ofNullable(filterVo.getTemplateOperation())
                .map(Enum::name).orElse(null);

        Specification<DataTestCase> specification = (Specification<DataTestCase>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =new ArrayList<>();

            if (StrUtil.isNotBlank(tableId)) {
                ListJoin<DataTestCase, MetaTableEntity> join =
                        root.join(root.getModel().getList("relatedTables", MetaTableEntity.class), JoinType.INNER);
                Predicate p = criteriaBuilder.equal(join.get("id").as(String.class), tableId);
                predicates.add(p);
            }

            if (StrUtil.isNotBlank(ownerId)) {
                Predicate p = criteriaBuilder.equal(root.get("ownerId").as(String.class), ownerId);
                predicates.add(p);
            }

            if (StrUtil.isNotBlank(ruleType)) {
                Predicate p = criteriaBuilder.equal(root.get("ruleType").as(String.class), ruleType);
                predicates.add(p);
            }

            if (StrUtil.isNotBlank(qualityProperty)) {
                Predicate p = criteriaBuilder.equal(root.get("qualityProperty").as(String.class), qualityProperty);
                predicates.add(p);
            }

            if (StrUtil.isNotBlank(templateOperation)) {
                Predicate p = criteriaBuilder.equal(root.get("templateOperation"), templateOperation);
                predicates.add(p);
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.isNotNull(root.get("id"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<DataTestCase> page = dataTestCaseRepository.findAll(specification, pageable);

        List<DataTestCase> cases = page.getContent();
        List<FetchRulesListResponseVo> fetchRulesListResponseVos = cases.stream()
                .map(c -> {
                    String oId = c.getOwnerId();
                    String name = Optional.ofNullable(oId)
                            .map(i -> usersRepository.getOne(i).getUsername()).orElse(null);
                    FetchRulesListResponseVo responseVo = modelMapper.map(c, FetchRulesListResponseVo.class);
                    responseVo.setOwnerName(name);
                    return responseVo;
                }
        ).collect(Collectors.toList());
        FetchRulesListResponsePageVo pageVo = new FetchRulesListResponsePageVo();
        pageVo.setFetchRulesListResponse(fetchRulesListResponseVos);

        pageVo.setPageNum(pageNum-1);
        pageVo.setPageSize(pageSize);
        pageVo.setTotalCount(page.getTotalElements());
        return pageVo;
    }

    @Override
    public CreateCustomizedRuleResponseVo updateCustomizedRule(CreateCustomizedRuleResponseVo updateRuleVo) {
        ModelMapper modelMapper = new ModelMapper();
        DataTestCase dataTestCase = modelMapper.map(updateRuleVo, DataTestCase.class);
        List<String> tags = updateRuleVo.getTags();
        dataTestCase.setRuleType(RuleTypeEnum.CUSTOMIZED_SQL);
        return modelMapper.map(dataTestCaseRepository.saveAndFlush(setTags(tags, dataTestCase)), CreateCustomizedRuleResponseVo.class);
    }

    @Override
    public CreatePresetRuleTemplateResponseVo updatePresetRule(CreatePresetRuleTemplateResponseVo updateVo) {

        DataTestCase dataTestCase = dataTestCaseRepository.getOne(updateVo.getId());
        Integer dbId = dataTestCase.getDbId();

        ModelMapper modelMapper = new ModelMapper();
        DataTestCase dataTestCaseUpdate = modelMapper.map(updateVo, DataTestCase.class);
        List<RelatedTableVO> tables = updateVo.getRelatedTables();
        String tableId = tables.get(0).getId();

        TemplateOperationEnum operation = updateVo.getTemplateOperation();

        TableVO table = tableService.getTable(tableId);
        List<String> fieldIds = updateVo.getFieldIds();

        List<String> fieldNames = fieldIds.stream()
                .map(i-> columnRepository.findById(i).get().getName())
                .collect(Collectors.toList());

        log.info(fieldNames.toString());

        List<CaseValidator> validators = updateVo.getValidateObject();
        List<String> tags = updateVo.getTags();

        List<String> fieldTmp = updateVo.getFieldIds();
        String[] operatedFieldIds = Convert.toStrArray(fieldTmp);

        String tableName = table.getName();
        List<String> fields = fieldIds.stream().map(i -> columnRepository.getOne(i).getName()).collect(Collectors.toList());

        Name tableQuery = SQLQueryHelper.getQualifiedTableName(tableName, table.getSchema().toUpperCase(), DBType.valueOf(table.getDbType())).unquotedName();
        tableName = tableQuery.toString();
        String sql = SqlGenerator.generateSqlByOperation(operation, updateVo.getIsTableLevel(), fields, tableName);

        dataTestCaseUpdate.setCaseSql(sql)
                .setQualityProperty(operation.getQualityProperty())
                .setTemplateType(operation.getTemplateType())
                .setTemplateOperation(updateVo.getTemplateOperation())
                .setFields(Convert.toStrArray(fieldNames))
                .setValidateObject(validators)
                .setRuleType(RuleTypeEnum.PRESET_TEMPLATE)
                .setOperatedFieldIds(operatedFieldIds)
                .setDbId(dbId)
                .setRelatedTables(Collections.singletonList(tableService.find(tableId)));

        dataTestCaseUpdate.setId(updateVo.getId());

        return modelMapper.map(dataTestCaseRepository.saveAndFlush(setTags(tags, dataTestCaseUpdate)), CreatePresetRuleTemplateResponseVo.class);
    }

    @Override
    public String deleteRule(String id) {
        dataTestCaseRepository.deleteById(id);
        return id;
    }

    @Override
    public List<Future<DataTestResult>> submitByCaseIds(@Validated @NotEmpty List<String> caseIds) {
        return executorUtil.submit(caseIds);
    }

    @Override
    public List<DataTestResult> getResultsByCaseId(String caseId) {
        return null;
    }

    @Override
    public List<CaseValidator> sqlValidate(String sql, List<CaseValidator> validators) {
        return null;
    }

    @Override
    public RuleDetailVo getRuleDetailById(String id) {
        DataTestCase dataTestCase = dataTestCaseRepository.getOne(id);
        ModelMapper modelMapper = new ModelMapper();
        RuleDetailVo ruleDetailVo = modelMapper.map(dataTestCase, RuleDetailVo.class);

        List<String> fieldIds = Optional.ofNullable(dataTestCase.getOperatedFieldIds()).map(
                Arrays::asList
        ).orElse(new ArrayList<>());
        ruleDetailVo.setFieldIds(fieldIds);
        return ruleDetailVo;
    }

    @Override
    public List<String> deleteRuleByIds(List<String> caseIds) {
        List<DataTestCase> dataTestCases = dataTestCaseRepository.findAllById(caseIds);
        dataTestCaseRepository.deleteAll(dataTestCases);
        return caseIds;
    }

    private DataTestCase setTags(List<String> tags, DataTestCase dataTestCase) {
        if (CollUtil.isEmpty(tags)) {
            return dataTestCase;
        }
        List<TagsEntity> findTags = tagService.findTags(tags);
        String[] tagsIds = Convert.toStrArray(findTags.stream().map(TagsEntity::getId).toArray());
        dataTestCase.setTags(findTags).setTagsIds(tagsIds);
        return dataTestCase;
    }
}

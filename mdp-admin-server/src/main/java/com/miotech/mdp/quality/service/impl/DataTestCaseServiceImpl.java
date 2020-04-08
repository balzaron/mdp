package com.miotech.mdp.quality.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.miotech.mdp.common.exception.InvalidQueryException;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.models.protobuf.metabase.Col;
import com.miotech.mdp.common.models.protobuf.metabase.QueryData;
import com.miotech.mdp.common.persistent.TagsRepository;
import com.miotech.mdp.common.service.DatabaseGeneralQueryService;
import com.miotech.mdp.common.service.TagService;
import com.miotech.mdp.quality.exception.RequestAttributeErrorException;
import com.miotech.mdp.quality.exception.ResourceNotFoundException;
import com.miotech.mdp.quality.models.bo.DataTestCaseValidate;
import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.entity.DataTestCase;
import com.miotech.mdp.quality.models.vo.test.*;
import com.miotech.mdp.quality.repository.DataTestCaseRepository;
import com.miotech.mdp.quality.service.DataTestCaseService;
import com.miotech.mdp.quality.util.AliasUtil;
import com.miotech.mdp.quality.util.SqlParserUtil;
import com.miotech.mdp.quality.util.Converter;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.dao.MetabaseDatabaseEntity;
import com.miotech.mdp.table.service.DatabaseService;
import com.miotech.mdp.table.service.TableService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.miotech.mdp.quality.util.Converter.convert2DataTestCaseVo;

/**
 * @author: shanyue.gao
 * @date: 2020/1/3 10:31 AM
 */

@Service
@Slf4j
public class DataTestCaseServiceImpl implements DataTestCaseService {

    private final String SEARCH_PATTERN =  "(?i).*limit\\s+(\\d+)$";
    private final Integer TEST_LIMIT = 10;
    private final String VALIDATION_PREFIX = "validate_";


    @Autowired
    private DataTestCaseRepository dataTestCaseRepository;

    @Autowired
    private DatabaseGeneralQueryService queryService;

    @Autowired
    private TableService tableService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private AliasUtil aliasUtil;

    @Autowired
    private TagsRepository tagsRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TagService tagService;

    @Override
    public DataTestCasePageVo retrieve(SearchCaseRequestVo searchCaseRequestVo) {
        val searchString = searchCaseRequestVo.getSearchString();
        val pageNum = searchCaseRequestVo.getPageNum();
        val pageSize = searchCaseRequestVo.getPageSize();
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);

        Page<DataTestCase> page = Optional.ofNullable(searchString)
                .map(s -> dataTestCaseRepository.findByNameContainingOrDescriptionContaining(s, s, pageable))
                .orElse(dataTestCaseRepository.findAll(pageable));

        DataTestCasePageVo pageVo = new DataTestCasePageVo();
        pageVo.setPageNum(page.getNumber());
        pageVo.setPageSize(page.getSize());
        pageVo.setTotalCount(page.getTotalElements());
        List<DataTestCase> testCases = page.getContent();

        List<DataTestCaseResultVo> testCaseResultVos = new ArrayList<>();

        testCases.forEach(e -> testCaseResultVos.add(new DataTestCaseResultVo()
                .setName(e.getName())
        .setUpdateTime(e.getUpdateTime())
        .setCaseSql(e.getCaseSql())
        .setDbId(e.getDbId())
        .setId(e.getId())
        .setDescription(e.getDescription())
        .setTags(e.getTags().stream().map(
                TagsEntity::getName
        ).collect(Collectors.toList()))
        .setRelatedTables(Converter.convert2RelatedTableVo(e.getRelatedTables()))));

        pageVo.setDataTestCases(testCaseResultVos);
        return pageVo;
    }

    @Override
    public DataTestCaseResultVo findById(String id) {
        DataTestCaseResultVo testCaseResultVo = dataTestCaseRepository.findById(id)
                .map(a->{
                    DataTestCaseResultVo resultVo = new DataTestCaseResultVo();
                    resultVo.setCaseSql(a.getCaseSql())
                    .setDescription(a.getDescription())
                    .setId(a.getId())
                    .setDbId(a.getDbId())
                    .setUpdateTime(a.getUpdateTime())
                    .setName(a.getName())
                    .setTags(a.getTags().stream().map(
                            TagsEntity::getName
                    ).collect(Collectors.toList()))
                    .setRelatedTables(Converter.convert2RelatedTableVo(a.getRelatedTables()));
                    return resultVo;
                })
                .orElseThrow(() -> new ResourceNotFoundException("No id " + id + "found!"));
        return testCaseResultVo;
    }

    @Override
    public String addCase(DataTestCaseRequestVo dataTestCaseRequestVo) {
        log.info("request view object is : " + dataTestCaseRequestVo);
        DataTestCase dataTestCase = new DataTestCase();
        return Optional.ofNullable(dataTestCaseRequestVo)
                .map(c -> {
                    String sql = dataTestCaseRequestVo.getCaseSql();
                    String[] fields = SqlParserUtil.getColumns(sql);
                    dataTestCase.setCaseSql(sql)
                            .setDescription(dataTestCaseRequestVo.getDescription())
                            .setName(dataTestCaseRequestVo.getName())
                            .setDbId(dataTestCaseRequestVo.getDbId())
                            .setFields(fields);

                    List<MetaTableEntity> tableEntityList = aliasUtil.alias2Table(dataTestCaseRequestVo.getRelatedTables());
                    List<TagsEntity> tagsEntityList = tagService.findTags(dataTestCaseRequestVo.getTags());

                    String[] tagIds = tagsEntityList.stream()
                            .map(TagsEntity::getId)
                            .toArray(String[]::new);
                    dataTestCase.setTags(tagsEntityList)
                            .setRelatedTables(tableEntityList)
                            .setTagsIds(tagIds);
                    DataTestCase res = dataTestCaseRepository.saveAndFlush(dataTestCase);
                    return res.getId();
                })
                .orElseThrow(() -> new RequestAttributeErrorException("request payload is incorrect!"));
    }

    @Override
    public String updateCase(String id, DataTestCaseRequestVo dataTestCaseVo) {
        return dataTestCaseRepository.findById(id)
                .map(c -> {
                    String sql =  dataTestCaseVo.getCaseSql();
                    String[] fields = SqlParserUtil.getColumns(sql);
                    c.setCaseSql(dataTestCaseVo.getCaseSql())
                            .setDescription(dataTestCaseVo.getDescription())
                            .setName(dataTestCaseVo.getName())
                            .setFields(fields)
                    .setDbId(dataTestCaseVo.getDbId());
                    List<MetaTableEntity> tableEntityList = aliasUtil.alias2Table(dataTestCaseVo.getRelatedTables());
                    List<TagsEntity> tagsEntityList = tagService.findTags(dataTestCaseVo.getTags());
                    String[] tagIds = tagsEntityList.stream()
                            .map(TagsEntity::getId)
                            .toArray(String[]::new);
                    c.setRelatedTables(tableEntityList)
                            .setTags(tagsEntityList).setTagsIds(tagIds)
                            .setId(id);
                    DataTestCase res = dataTestCaseRepository.saveAndFlush(c);
                     return res.getId();
                }).orElseThrow(() -> new ResourceNotFoundException("Case not found with id " + id));

    }

    @Override
    public String deleteCase(String id) {
        DataTestCase dataTestCase = dataTestCaseRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("The id: "+id+"not found!"));
        dataTestCaseRepository.delete(dataTestCase);
        return id;
    }

    @Override
    public String deleteCases(List<String> ids) {
        List<DataTestCase> dataTestCases = new ArrayList<>();
        ids.forEach(
                id -> dataTestCases.add(dataTestCaseRepository.findById(id).orElse(null))
        );
        return Optional.of(dataTestCases)
                .map((d)-> {
                    dataTestCaseRepository.deleteAll(d);
                    return "success!";
                })
                .orElseThrow(() -> new ResourceNotFoundException("no resource found"));
    }

    @Override
    public DataTestCaseValidationResult validateCase(DataTestCaseValidate dataTestCaseValidate) {
        try {
            String sql = dataTestCaseValidate.getCaseSql();
            MetabaseDatabaseEntity database = databaseService.getDatabase(dataTestCaseValidate.getDbId());
            if (!sql.trim().matches(SEARCH_PATTERN)) {
                sql = sql.replace(";", " ");
                sql  = String.format("%s LIMIT %s", sql, TEST_LIMIT);
            }
            QueryData data = queryService.queryData(database.getId()
            , sql);
            if (data.getRowsList().isEmpty()) {
                throw new RequestAttributeErrorException("Invalid query: No result found");
            }

            List<CaseValidator> validation = dataTestCaseValidate.getValidateObject();
            if (validation == null || validation.isEmpty()) {
                List<Col> cols = data.getColsList();
                List<String> validationList = IntStream.range(0, cols.size())
                        .filter(i ->  {
                            Col col = cols.get(i);

                            return col.getName().toLowerCase().startsWith(VALIDATION_PREFIX) &&
                                    col.getBaseType().toLowerCase().equals("type/boolean");
                        })
                        .mapToObj(i -> String.format("{\"fieldName\": \"%s\", \"operator\":\"==\", \"expected\":true}", cols.get(i).getName()))
                        .collect(Collectors.toList());
                if (!validationList.isEmpty()) {
                    String validationStr = String.format( "{\"validation\": [%s]}",
                            Strings.join(validationList, ','));

                    JSONArray array = JSONObject.parseObject(validationStr).getJSONArray("validation");
                    validation = array.toJavaList(CaseValidator.class);
                }
            }

            List<RelatedTableVO> relatedTables = tableService.getRelatedTableFromQuery(database.getId(), sql)
                    .stream().map(this::toTableVO).collect(Collectors.toList());
            DataTestCaseValidationResult result = new DataTestCaseValidationResult();
            result.setCaseSql(dataTestCaseValidate.getCaseSql());
            result.setDbId(dataTestCaseValidate.getDbId());
            result.setValidateObject(validation);
            result.setRelatedTables(relatedTables);
            return result;
        } catch (InvalidQueryException e) {
            throw new RequestAttributeErrorException("Invalid query: " + e.getMessage());
        }

    }

    @Override
    public List<DataTestCaseVo> getTableTestCases(String tableId) {
        return convert2DataTestCaseVo(dataTestCaseRepository.findByRelatedTableId(tableId));
    }

    @Override
    public void deleteAll() {
        dataTestCaseRepository.deleteAll();
    }

    private RelatedTableVO toTableVO(MetaTableEntity tableEntity) {
        RelatedTableVO tableVO = new RelatedTableVO();
        tableVO.setDbType(tableEntity.getDatabaseType());
        tableVO.setId(tableEntity.getId());
        tableVO.setLifecycle(tableEntity.getLifecycle());
        tableVO.setName(tableEntity.getName());
        tableVO.setSchema(tableEntity.getSchema());
        return tableVO;
    }
}

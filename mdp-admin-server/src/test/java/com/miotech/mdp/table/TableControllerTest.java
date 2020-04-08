package com.miotech.mdp.table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.mdp.ServiceTest;
import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.vo.TableVO;
import com.miotech.mdp.table.persistence.TableRepository;
import org.assertj.core.util.Arrays;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testng.Assert;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class TableControllerTest extends ServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TableRepository tableRepository;

    private Set<String> needClearTables = new HashSet<>();

    @After
    public void clearTable() {
        for (String needClearTable : needClearTables) {
            deleteTable(needClearTable);
        }
    }

    @Test
    public void testGetTable() throws Exception {
        MetaTableEntity tableEntity = new MetaTableEntity();
        tableEntity.setName("get-table-test");
        tableEntity.setDbId(5);
        tableEntity.setRefDbIds(Arrays.array(5));
        String id = createTable(tableEntity);
        needClearTables.add(id);
        String response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/table/" + id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        TableVO vo = transferToTableVO(response).getResult();
        String getId = vo.getId();
        Assert.assertEquals(getId, id);
    }

    @Test
    public void testCreateTable() throws Exception {
        /*String tableName = UUID.randomUUID().toString();
        TableInfo tableInfo = TableInfo.builder()
                .tableName(tableName)
                .dbType("mysql")
                .schema(tableName)
                .currentDBId(5)
                .referenceDBIds(Lists.newArrayList(5))
                .lifecycle(Lifecycle.COLLECTION.getName())
                .build();
        String response = mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/table/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tableInfo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        TableVO vo = transferToTableVO(response).getResult();
        Assert.assertTrue(StringUtils.isNotEmpty(vo.getId()));
        needClearTables.add(vo.getId());
        String createName = vo.getName();
        Assert.assertEquals(createName, tableName);*/
    }

    private String createTable(MetaTableEntity metaTableEntity) {
        return tableRepository.saveAndFlush(metaTableEntity).getId();
    }

    private void deleteTable(String id) {
        tableRepository.deleteById(id);
    }

    private Result<TableVO> transferToTableVO(String response) throws JsonProcessingException {
        return objectMapper.readValue(response, new TypeReference<Result<TableVO>>() {});
    }
}

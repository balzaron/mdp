package com.miotech.mdp.table.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.mdp.table.model.dao.MetaTableRevisionEntity;
import com.miotech.mdp.table.model.vo.TableVO;
import com.miotech.mdp.table.persistence.TableRevisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TableRevisionService {

    @Autowired
    TableRevisionRepository tableRevisionRepository;

    public MetaTableRevisionEntity createTableRevision(TableVO tableVO) throws JsonProcessingException {
        String tableInfo = new ObjectMapper().writeValueAsString(tableVO);
        return createTableRevision(tableVO.getId(), tableInfo);
    }

    private MetaTableRevisionEntity createTableRevision(String tableId, String table) {
        MetaTableRevisionEntity entity = new MetaTableRevisionEntity();
        entity.setTableId(tableId);
        entity.setDetails(table);
        entity.setCreateTime(LocalDateTime.now());

        return tableRevisionRepository.saveAndFlush(entity);
    }
}

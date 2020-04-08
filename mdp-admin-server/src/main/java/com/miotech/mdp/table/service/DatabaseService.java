package com.miotech.mdp.table.service;

import com.miotech.mdp.common.exception.ResourceNotFoundException;
import com.miotech.mdp.table.model.bo.DatabaseInfo;
import com.miotech.mdp.table.model.dao.MetabaseDatabaseEntity;
import com.miotech.mdp.table.model.vo.DatabaseVO;
import com.miotech.mdp.table.persistence.MetabaseDatabaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseService {

    @Autowired
    MetabaseDatabaseRepository metabaseDatabaseRepository;

    public MetabaseDatabaseEntity getDatabase(Integer id) {
        return metabaseDatabaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Database"));
    }

    public List<MetabaseDatabaseEntity> getDatabases(DatabaseInfo databaseInfo) {
        MetabaseDatabaseEntity searchEntity = new MetabaseDatabaseEntity();
        searchEntity.setId(databaseInfo.getId());
        searchEntity.setEngine(databaseInfo.getDbType());
        Example<MetabaseDatabaseEntity> example = Example.of(searchEntity, ExampleMatcher.matching().withIgnoreNullValues());
        return metabaseDatabaseRepository.findAll(example);
    }

    public List<MetabaseDatabaseEntity> getDatabases(List<Integer> databaseIds) {
        return metabaseDatabaseRepository.findAllById(databaseIds);
    }

    public List<String> getDatabaseTypes() {
        return metabaseDatabaseRepository.findDistinctDatabaseType();
    }

    public List<DatabaseVO> convertToDatabaseVOs(List<MetabaseDatabaseEntity> metabaseDatabaseEntities) {
        return metabaseDatabaseEntities.stream()
                .map(this::convertToDatabaseVO)
                .collect(Collectors.toList());
    }

    public DatabaseVO convertToDatabaseVO(MetabaseDatabaseEntity metabaseDatabaseEntity) {
        DatabaseVO databaseVO = new DatabaseVO();
        databaseVO.setId(metabaseDatabaseEntity.getId());
        databaseVO.setType(metabaseDatabaseEntity.getEngine());
        databaseVO.setName(metabaseDatabaseEntity.getName());
        return databaseVO;
    }
}

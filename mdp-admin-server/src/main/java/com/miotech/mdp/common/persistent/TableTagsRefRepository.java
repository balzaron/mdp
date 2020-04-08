package com.miotech.mdp.common.persistent;

import com.miotech.mdp.common.model.dao.TableTagsRefEntity;
import com.miotech.mdp.common.model.dao.TableTagsRefId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableTagsRefRepository extends JpaRepository<TableTagsRefEntity, TableTagsRefId> {
}

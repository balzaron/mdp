package com.miotech.mdp.common.persistent;

import com.miotech.mdp.common.model.dao.TagsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagsRepository extends JpaRepository<TagsEntity, String> {
}

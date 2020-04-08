package com.miotech.mdp.common.model.dao;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "meta_table_tags_ref", schema = "public", catalog = "mdp")
@Data
public class TableTagsRefEntity {

    @EmbeddedId
    TableTagsRefId tableTagsRefId;

}

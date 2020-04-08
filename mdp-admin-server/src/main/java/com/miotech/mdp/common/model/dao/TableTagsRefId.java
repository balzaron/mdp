package com.miotech.mdp.common.model.dao;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class TableTagsRefId implements Serializable {

    private String tableId;

    private String tagId;
}

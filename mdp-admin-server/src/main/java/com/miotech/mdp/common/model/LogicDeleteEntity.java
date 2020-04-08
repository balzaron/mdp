package com.miotech.mdp.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreRemove;

@MappedSuperclass
public class LogicDeleteEntity extends BaseEntity {
    @Column(name = "deleted")
    @JsonIgnore
    private Integer deleted = 0;

    @PreRemove
    public void deleteEntity() {
        this.deleted = 1;
    }
}

package com.miotech.mdp.common.model.dao;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tags", schema = "public", catalog = "mdp")
@Data
@GenericGenerator(name = "custom_id", strategy = "com.miotech.mdp.common.jpa.CustomIdentifierGenerator")
public class TagsEntity {

    @Id
    @GeneratedValue(generator = "custom_id")
    @Column(name = "id")
    private String id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name =  "color")
    private String color;
}

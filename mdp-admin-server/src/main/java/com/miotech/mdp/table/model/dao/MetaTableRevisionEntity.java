package com.miotech.mdp.table.model.dao;


import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meta_table_revision", schema = "public", catalog = "mdp")
@Data
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class, defaultForType = String.class),
        @TypeDef(name = "intArray", typeClass = IntArrayType.class, defaultForType = Integer[].class),
        @TypeDef(name = "stringArray", typeClass = StringArrayType.class, defaultForType = String[].class)
})
@GenericGenerator(name = "custom_id", strategy = "com.miotech.mdp.common.jpa.CustomIdentifierGenerator")
public class MetaTableRevisionEntity {
    @Id
    @GeneratedValue(generator = "custom_id")
    @Column(name = "id")
    private String id;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name="details")
    private String details;

    @Column(name="table_id")
    private String tableId;

    @Column(name="remark")
    private String remark;
}

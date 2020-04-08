package com.miotech.mdp.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;


@Data
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class, defaultForType = String.class),
        @TypeDef(name = "json", typeClass = JsonStringType.class, defaultForType = String.class),
        @TypeDef(name = "int-array", typeClass = IntArrayType.class, defaultForType = Integer[].class),
        @TypeDef(name = "string-array", typeClass = StringArrayType.class, defaultForType = String[].class)
})
@MappedSuperclass
public class BaseEntity {
    @Id
    @GenericGenerator(name = "custom_id", strategy = "com.miotech.mdp.common.jpa.CustomIdentifierGenerator")
    @GeneratedValue(generator = "custom_id")
    @Column(name = "id", columnDefinition = "varchar(50)")
    private String id;

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updateTime = LocalDateTime.now();
}
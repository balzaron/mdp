package com.miotech.mdp.common.model.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.miotech.mdp.common.model.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "public")
@Data
@GenericGenerator(name = "custom_id", strategy = "com.miotech.mdp.common.jpa.CustomIdentifierGenerator")

public class UsersEntity extends BaseEntity {
    @Id
    @GeneratedValue(generator = "custom_id")
    @Column(name = "id",columnDefinition = "varchar (200)")
    private String id;

    @Column(name = "user_name")
    private String username;

    @Column(name = "user_info")
    private String userInfo;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updateTime = LocalDateTime.now();
}

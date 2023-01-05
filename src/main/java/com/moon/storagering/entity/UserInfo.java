package com.moon.storagering.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Entity
@Getter
@Setter
public class UserInfo {

    @Id
    private String id;

    private String userName;

    private String password;

    private String systemRole;

    private Date createTime;

    private String email;

    private int level;
}

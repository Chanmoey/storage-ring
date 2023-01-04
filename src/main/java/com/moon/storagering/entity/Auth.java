package com.moon.storagering.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Entity
@Getter
@Setter
public class Auth {

    @Id
    private String id;

    private String bucketName;

    private String token;

    private Date authTime;

    public void generateId() {
        this.id = UUID.randomUUID().toString().replace("-", "");
    }
}

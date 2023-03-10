package com.moon.storagering.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class Bucket {

    @Id
    private String id;

    private String bucketName;

    private Date createTime;

    private String detail;

    private String creator;

    public Bucket(String bucketName, String creator, String detail) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.bucketName = bucketName;
        this.creator = creator;
        this.detail = detail;
        this.createTime = new Date();
    }
}

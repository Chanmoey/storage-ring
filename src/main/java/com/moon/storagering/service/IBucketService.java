package com.moon.storagering.service;

import com.moon.storagering.entity.Bucket;
import com.moon.storagering.entity.UserInfo;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public interface IBucketService {

    boolean addBucket(UserInfo userInfo, String bucketName, String detail);

    boolean deleteBucket(String bucketName);

    boolean updateBucket(Bucket bucket);

    Bucket getBucketById(String bucketId);

    Bucket getBucketByName(String bucketName);

    List<Bucket> getBucketsByCreator(String creator);

    List<Bucket> getUserBuckets(String token);
}

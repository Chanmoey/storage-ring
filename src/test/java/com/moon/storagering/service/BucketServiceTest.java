package com.moon.storagering.service;

import com.moon.storagering.entity.Bucket;
import com.moon.storagering.entity.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@SpringBootTest
class BucketServiceTest {

    @Autowired
    IBucketService bucketService;

    @Autowired
    IUserInfoService userInfoService;

    @Test
    void addBucketTest() {
        UserInfo userInfo = userInfoService.getUserInfoByUserName("Joey");
        bucketService.addBucket(userInfo, "testBucket", "this is a test bucket");
    }

    @Test
    void getBucketTest() {
        Bucket bucket = bucketService.getBucketByName("testBucket");
        System.out.println(bucket.getCreator());
    }
}

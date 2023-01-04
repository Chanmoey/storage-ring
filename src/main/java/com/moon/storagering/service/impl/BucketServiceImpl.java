package com.moon.storagering.service.impl;

import com.moon.storagering.entity.Auth;
import com.moon.storagering.entity.Bucket;
import com.moon.storagering.entity.UserInfo;
import com.moon.storagering.repository.BucketRepository;
import com.moon.storagering.service.IAuthService;
import com.moon.storagering.service.IBucketService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Transactional
@Service
public class BucketServiceImpl implements IBucketService {

    @Autowired
    BucketRepository bucketRepository;

    @Autowired
    IAuthService authService;

    @Override
    public boolean addBucket(UserInfo userInfo, String bucketName, String detail) {
        Bucket bucket = new Bucket(bucketName, userInfo.getUserName(), detail);
        this.bucketRepository.save(bucket);

        // 添加User和Bucket的授权
        Auth auth = new Auth();
        auth.generateId();
        auth.setAuthTime(new Date());
        auth.setToken(userInfo.getId());
        auth.setBucketName(bucketName);
        authService.addAuth(auth);
        return true;
    }

    @Override
    public boolean deleteBucket(String bucketName) {
        bucketRepository.deleteBucketByBucketName(bucketName);
        // 删除bucket相关的授权
        authService.deleteAuthByBucketName(bucketName);
        return true;
    }

    @Override
    public boolean updateBucket(Bucket bucket) {
        bucketRepository.save(bucket);
        return true;
    }

    @Override
    public Bucket getBucketById(String bucketId) {
        return bucketRepository.findById(bucketId).orElseThrow();
    }

    @Override
    public Bucket getBucketByName(String bucketName) {
        return bucketRepository.findBucketByBucketName(bucketName).orElseThrow();
    }

    @Override
    public List<Bucket> getBucketsByCreator(String creator) {
        return bucketRepository.findAllByCreator(creator);
    }

    @Override
    public List<Bucket> getUserBuckets(String token) {
        List<Auth> auths = authService.getAllAuthByToken(token);
        List<String> bucketNames = auths.stream().map(Auth::getBucketName).toList();
        return bucketRepository.findAllByBucketNameIn(bucketNames);
    }
}

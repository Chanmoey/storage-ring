package com.moon.storagering.service;

import com.moon.storagering.entity.Auth;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public interface IAuthService {

    void addAuth(Auth auth);

    void deleteAuthByBucketName(String bucketName);

    List<Auth> getAllAuthByToken(String token);
}

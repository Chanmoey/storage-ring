package com.moon.storagering.service.impl;

import com.moon.storagering.entity.Auth;
import com.moon.storagering.repository.AuthRepository;
import com.moon.storagering.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    AuthRepository authRepository;

    @Override
    public void addAuth(Auth auth) {
        authRepository.save(auth);
    }

    @Override
    public void deleteAuthByBucketName(String bucketName) {
        authRepository.deleteAllByBucketName(bucketName);
    }

    @Override
    public List<Auth> getAllAuthByToken(String token) {
        return authRepository.findAllByToken(token);
    }
}

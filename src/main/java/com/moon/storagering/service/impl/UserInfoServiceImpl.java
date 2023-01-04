package com.moon.storagering.service.impl;

import com.moon.storagering.entity.UserInfo;
import com.moon.storagering.repository.UserInfoRepository;
import com.moon.storagering.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Service
public class UserInfoServiceImpl implements IUserInfoService {

    @Autowired
    UserInfoRepository userInfoRepository;

    @Override
    public UserInfo getUserInfoByUserName(String userName) {
        return userInfoRepository.findByUserName(userName).orElseThrow();
    }
}

package com.moon.storagering.service;

import com.moon.storagering.entity.UserInfo;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public interface IUserInfoService {

    UserInfo getUserInfoByUserName(String name);
}

package com.moon.storagering.repository;

import com.moon.storagering.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

    Optional<UserInfo> findByUserName(String userName);
}

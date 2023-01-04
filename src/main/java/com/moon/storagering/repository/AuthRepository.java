package com.moon.storagering.repository;

import com.moon.storagering.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Repository
public interface AuthRepository extends JpaRepository<Auth, String> {

    List<Auth> findAllByBucketName(String bucketName);

    void deleteAllByBucketName(String bucketName);

    List<Auth> findAllByToken(String token);
}

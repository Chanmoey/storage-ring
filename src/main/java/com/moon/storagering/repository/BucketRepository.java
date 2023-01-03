package com.moon.storagering.repository;

import com.moon.storagering.entity.Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Repository
public interface BucketRepository extends JpaRepository<Bucket, String> {

    void deleteBucketByBucketName(String bucketName);

}

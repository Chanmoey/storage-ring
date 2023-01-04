package com.moon.storagering.repository;

import com.moon.storagering.entity.Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Repository
public interface BucketRepository extends JpaRepository<Bucket, String> {

    void deleteBucketByBucketName(String bucketName);

    Optional<Bucket> findBucketByBucketName(String bucketName);

    List<Bucket> findAllByCreator(String creator);

    List<Bucket> findAllByBucketNameIn(List<String> bucketNames);
}

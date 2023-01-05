package com.moon.storagering.service;

import com.moon.storagering.entity.ObjectListResult;
import com.moon.storagering.entity.StorageRingObject;
import com.moon.storagering.entity.StorageRingObjectSummary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * @author Chanmoey
 * @date 2023年01月05日
 */
public interface IStorageRingStore {

    void createBucketStore(String bucketName) throws IOException;

    void deleteBucketStore(String bucketName) throws IOException;

    void createSeqTable() throws IOException;

    void put(String bucketName, String key, ByteBuffer content,
             long length, String mediaType, Map<String, String> properties) throws Exception;


    StorageRingObjectSummary getSummary(String bucketName, String key) throws IOException;

    List<StorageRingObjectSummary> list(String bucketName, String startKey, String endKey) throws IOException;

    ObjectListResult listDir(String bucketName, String dir, String start, int maxCount) throws IOException;

    ObjectListResult listByPrefix(String bucketName, String dir, String start, String prefix, int maxCount) throws IOException;

    StorageRingObject getObject(String bucketName, String key) throws IOException;

    void deleteObject(String bucketName, String key) throws Exception;

}

package com.moon.storagering.service.impl;

import com.google.common.base.Strings;
import com.moon.storagering.common.util.JsonUtil;
import com.moon.storagering.common.util.StorageRingUtil;
import com.moon.storagering.entity.ObjectListResult;
import com.moon.storagering.entity.ObjectMetaData;
import com.moon.storagering.entity.StorageRingObject;
import com.moon.storagering.entity.StorageRingObjectSummary;
import com.moon.storagering.exception.bussness.FileSystemException;
import com.moon.storagering.service.IHdfsService;
import com.moon.storagering.service.IStorageRingStore;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.io.ByteBufferInputStream;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author Chanmoey
 * @date 2023年01月05日
 */
public class StorageRingStoreImpl implements IStorageRingStore {

    private static final Logger LOGGER = Logger.getLogger(StorageRingStoreImpl.class);

    private Connection connection = null;

    private IHdfsService hdfsService;

    private String zkUrls;

    private CuratorFramework zkClient;

    public StorageRingStoreImpl(Connection connection, IHdfsService hdfsService, String zkUrls) {
        this.connection = connection;
        this.hdfsService = hdfsService;
        this.zkUrls = zkUrls;

        zkClient = CuratorFrameworkFactory.newClient(zkUrls, new ExponentialBackoffRetry(20, 5));
        this.zkClient.start();
    }

    @Override
    public void createBucketStore(String bucketName) throws IOException {
        // 创建目录表
        HBaseService.createTable(connection, StorageRingUtil.getDirTableName(bucketName), StorageRingUtil.getDirColumnFamily(), null);

        // 创建文件表
        HBaseService.createTable(connection, StorageRingUtil.getObjTableName(bucketName), StorageRingUtil.getObjColumnFamily(), StorageRingUtil.OBJ_REGIONS);

        // 将其添加到seq表
        Put put = new Put(bucketName.getBytes());
        put.addColumn(StorageRingUtil.BUCKET_DIR_SEQ_CF_BYTES, StorageRingUtil.BUCKET_DIR_SEQ_QUALIFIER, Bytes.toBytes(0L));
        HBaseService.putRow(connection, StorageRingUtil.BUCKET_DIR_SEQ_TABLE, put);

        // 创建hdfs目录
        hdfsService.mkDir(StorageRingUtil.FILE_STORE_ROOT + "/" + bucketName);
    }

    @Override
    public void deleteBucketStore(String bucketName) throws IOException {
        // 删除目录表和文件表
        HBaseService.deleteTable(connection, StorageRingUtil.getDirTableName(bucketName));
        HBaseService.deleteTable(connection, StorageRingUtil.getObjTableName(bucketName));

        // 删除seq表中的记录
        HBaseService.deleteRow(connection, StorageRingUtil.BUCKET_DIR_SEQ_TABLE, bucketName);

        // 删除hdfs上的目录
        hdfsService.deleteDir(StorageRingUtil.FILE_STORE_ROOT + "/" + bucketName);

    }

    @Override
    public void createSeqTable() throws IOException {
        HBaseService.createTable(connection, StorageRingUtil.BUCKET_DIR_SEQ_TABLE, new String[]{StorageRingUtil.BUCKET_DIR_SEQ_CF}, null);
    }

    @Override
    public void put(String bucketName, String key, ByteBuffer content, long length, String mediaType, Map<String, String> properties) throws Exception {
        InterProcessMutex lock = null;

        // 判断是否创建目录
        if (key.endsWith("/")) {
            putDir(bucketName, key);
            return;
        }

        // 获取seqid
        String dir = key.substring(0, key.lastIndexOf("/") + 1);
        String hash = null;
        while (hash == null) {
            if (!dirExist(bucketName, dir)) {
                hash = putDir(bucketName, dir);
            } else {
                hash = getDirSeqId(bucketName, dir);
            }
        }

        // 上传文件到文件表

        // 获取锁
        String lockKey = key.replace("/", "_");
        lock = new InterProcessMutex(zkClient, "/stroagering/" + bucketName + "/" + lockKey);
        lock.acquire();

        // 上传文件
        String fileKey = hash + "_" + key.substring(key.lastIndexOf("/") + 1);
        Put contentPut = new Put(fileKey.getBytes());

        // 记录文件属性
        if (!Strings.isNullOrEmpty(mediaType)) {
            contentPut.addColumn(StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_CONT_QUALIFIER, mediaType.getBytes());
        }

        if (properties != null) {
            String prop = JsonUtil.toJson(properties);
            contentPut.addColumn(StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_PROPS_QUALIFIER, prop.getBytes());
        }

        contentPut.addColumn(StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_LEN_QUALIFIER, Bytes.toBytes(length));

        // 判断文件大小，小于20M，存储到hbase，否则存储到hdfs

        if (length <= StorageRingUtil.FILE_STORE_THRESHOLD) {
            ByteBuffer buffer = ByteBuffer.wrap(StorageRingUtil.OBJ_CONT_QUALIFIER);
            contentPut.addColumn(StorageRingUtil.OBJ_CONT_CF_BYTES, buffer, System.currentTimeMillis(), content);
            buffer.clear();
        } else {
            String fileDir = StorageRingUtil.FILE_STORE_ROOT + "/" + bucketName + "/" + hash;
            String name = key.substring(key.lastIndexOf("/") + 1);

            InputStream inputStream = new ByteBufferInputStream(content);
            hdfsService.saveFile(fileDir, name, inputStream, length, (short) 1);
        }

        HBaseService.putRow(connection, StorageRingUtil.getObjTableName(bucketName), contentPut);
        // 释放锁
        if (lock != null) {
            lock.release();
        }
    }

    @Override
    public StorageRingObjectSummary getSummary(String bucketName, String key) throws IOException {

        // 判断是否时文件夹
        if (key.endsWith("/")) {
            Result result = HBaseService.getRow(connection, StorageRingUtil.getDirTableName(bucketName), key);
            if (!result.isEmpty()) {
                // 读取文件夹的基础属性
                return this.dirObject2Summary(result, bucketName, key);
            }
            return null;
        }

        // 获取文件
        // 先获取文件的父目录
        String dir = key.substring(0, key.lastIndexOf("/") + 1);
        String seq = getDirSeqId(bucketName, dir);

        // 父目录不存在，则说明文件也不存在
        if (seq == null) {
            return null;
        }

        // 获取文件的rootKey
        String objectKey = seq + "_" + key.substring(key.lastIndexOf("/") + 1);

        Result result = HBaseService.getRow(connection, StorageRingUtil.getObjTableName(bucketName), objectKey);
        if (result.isEmpty()) {
            return null;
        }

        return this.result2ObjectSummary(result, bucketName, dir);
    }

    @Override
    public List<StorageRingObjectSummary> list(String bucketName, String startKey, String endKey) throws IOException {
        return null;
    }

    @Override
    public ObjectListResult listDir(String bucketName, String dir, String start, int maxCount) throws IOException {
        // 查询目录表

        start = Strings.nullToEmpty(start);
        Get get = new Get(Bytes.toBytes(dir));
        get.addFamily(StorageRingUtil.DIR_SUBDIR_CF_BYTES);

        if (!Strings.isNullOrEmpty(start)) {
            get.setFilter(new QualifierFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL,
                    new BinaryComparator(Bytes.toBytes(start))));
        }
        Result dirResult = HBaseService.getRow(connection, StorageRingUtil.getDirTableName(bucketName), get);
        List<StorageRingObjectSummary> subDirs = null;

        if (!dirResult.isEmpty()) {
            subDirs = new ArrayList<>();
            for (Cell cell : dirResult.rawCells()) {
                StorageRingObjectSummary summary = new StorageRingObjectSummary();
                byte[] qualifierBytes = new byte[cell.getQualifierLength()];
                CellUtil.copyQualifierTo(cell, qualifierBytes, 0);
                String name = Bytes.toString(qualifierBytes);
                summary.setKey(dir + name + "/");
                summary.setName(name);
                summary.setLastModifyTime(cell.getTimestamp());
                summary.setMediaType("");
                summary.setBucket(bucketName);
                summary.setLength(0);
                subDirs.add(summary);
                if (subDirs.size() >= maxCount + 1) {
                    break;
                }
            }
        }

        // 查询文件表
        String dirSeq = getDirSeqId(bucketName, dir);
        byte[] objStart = Bytes.toBytes(dirSeq + "_" + start);
        Scan objScan = new Scan();
        objScan.setStartRow(objStart);
        objScan.setRowPrefixFilter(Bytes.toBytes(dirSeq + "_"));
        objScan.setMaxResultsPerColumnFamily(maxCount + 1);
        objScan.addFamily(StorageRingUtil.OBJ_META_CF_BYTES);
        ResultScanner scanner = HBaseService.getScanner(connection, StorageRingUtil.getObjTableName(bucketName), objScan);
        List<StorageRingObjectSummary> objectSummaryList = new ArrayList<>();
        Result result = null;
        while (objectSummaryList.size() < maxCount + 2 && (result = scanner.next()) != null) {
            StorageRingObjectSummary summary = this.result2ObjectSummary(result, bucketName, dir);
            objectSummaryList.add(summary);
        }
        if (scanner != null) {
            scanner.close();
        }
        LOGGER.info("scan complete: " + Bytes.toString(objStart) + " - ");
        if (subDirs != null && subDirs.size() > 0) {
            objectSummaryList.addAll(subDirs);
        }

        Collections.sort(objectSummaryList);
        ObjectListResult listResult = new ObjectListResult();
        StorageRingObjectSummary nextMarkerObj =
                objectSummaryList.size() > maxCount ? objectSummaryList.get(objectSummaryList.size() - 1)
                        : null;
        if (nextMarkerObj != null) {
            listResult.setNextMarker(nextMarkerObj.getKey());
        }
        if (objectSummaryList.size() > maxCount) {
            objectSummaryList = objectSummaryList.subList(0, maxCount);
        }
        listResult.setMaxKeyNumber(maxCount);
        if (objectSummaryList.size() > 0) {
            listResult.setMinKey(objectSummaryList.get(0).getKey());
            listResult.setMaxKey(objectSummaryList.get(objectSummaryList.size() - 1).getKey());
        }
        listResult.setObjectCount(objectSummaryList.size());
        listResult.setObjectSummaries(objectSummaryList);
        listResult.setBucket(bucketName);

        return listResult;
    }

    @Override
    public ObjectListResult listByPrefix(String bucketName, String dir, String start, String prefix, int maxCount) throws IOException {
        return null;
    }

    @Override
    public StorageRingObject getObject(String bucketName, String key) throws IOException {
        // 判断是否为目录
        if (key.endsWith("/")) {
            Result result = HBaseService.getRow(connection, StorageRingUtil.getDirTableName(bucketName), key);
            if (result.isEmpty()) {
                return null;
            }
            ObjectMetaData objectMetaData = new ObjectMetaData();
            objectMetaData.setBucket(bucketName);
            objectMetaData.setKey(key);
            objectMetaData.setLength(0);
            objectMetaData.setLastModifyTime(result.rawCells()[0].getTimestamp());
            StorageRingObject object = new StorageRingObject();
            object.setMetaData(objectMetaData);
            return object;
        }

        // 读取文件
        // 获取父目录
        String dir = key.substring(0, key.lastIndexOf("/") + 1);
        String seq = getDirSeqId(bucketName, dir);

        // 父目录不存在，则说明文件也不存在
        if (seq == null) {
            return null;
        }

        // 获取文件的rootKey
        String objectKey = seq + "_" + key.substring(key.lastIndexOf("/") + 1);
        Result result = HBaseService.getRow(connection, StorageRingUtil.getObjTableName(bucketName), objectKey);

        if (result.isEmpty()) {
            return null;
        }

        ObjectMetaData objectMetaData = new ObjectMetaData();
        objectMetaData.setBucket(bucketName);
        objectMetaData.setKey(key);
        long len = Bytes.toLong(result.getValue(StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_LEN_QUALIFIER));

        // 长度
        objectMetaData.setLength(len);
        objectMetaData.setLastModifyTime(result.rawCells()[0].getTimestamp());

        // 类型
        objectMetaData.setMediaType(Bytes.toString(result.getValue(
                StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_MEDIA_TYPE_QUALIFIER
        )));

        // 属性
        byte[] p = result.getValue(
                StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_PROPS_QUALIFIER
        );
        if (p != null) {
            objectMetaData.setAttrs(JsonUtil.fromJson(Map.class, Bytes.toString(p)));
        }

        StorageRingObject object = new StorageRingObject();
        object.setMetaData(objectMetaData);

        // 读取文件内容
        if (result.containsNonEmptyColumn(StorageRingUtil.OBJ_CONT_CF_BYTES, StorageRingUtil.OBJ_CONT_QUALIFIER)) {
            // 从Hbase读取
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    result.getValue(StorageRingUtil.OBJ_CONT_CF_BYTES, StorageRingUtil.OBJ_CONT_QUALIFIER));
            object.setContent(inputStream);
        } else {
            // 从HDFS读取
            String fileDir = StorageRingUtil.FILE_STORE_ROOT + "/" + bucketName + "/" + seq;
            InputStream inputStream = hdfsService.openFile(fileDir, key.substring(key.lastIndexOf("/") + 1));
            object.setContent(inputStream);
        }

        return object;
    }

    @Override
    public void deleteObject(String bucketName, String key) throws Exception {
        if (key.endsWith("/")) {
            if (!isDirEmpty(bucketName, key)) {
                throw new FileSystemException(21002);
            }

            // 获取锁
            InterProcessMutex lock = null;
            String lockKey = key.replace("/", "_");
            lock = new InterProcessMutex(zkClient, "/stroagering/" + bucketName + "/" + lockKey);
            lock.acquire();

            // 从父目录删除数据
            String fatherDir = key.substring(0, key.lastIndexOf("/"));
            String name = fatherDir.substring(key.lastIndexOf("/"));

            if (name.length() > 0) {
                String parent = key.substring(0, key.lastIndexOf(name));
                HBaseService.deleteColumnQualifier(connection, StorageRingUtil.getDirTableName(bucketName),
                        parent, StorageRingUtil.DIR_SUBDIR_CF, name);
            }

            // 从目录表删除掉
            HBaseService.deleteRow(connection, StorageRingUtil.getDirTableName(bucketName), key);

            lock.release();
            return;
        }

        // 删除文件
        String dir = key.substring(0, key.lastIndexOf("/") + 1);
        String name = key.substring(key.lastIndexOf("/") + 1);
        String seqId = getDirSeqId(bucketName, dir);
        String objKey = seqId + "_" + name;

        // 获取文件长度
        Get get = new Get(objKey.getBytes());
        get.addColumn(StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_LEN_QUALIFIER);
        Result result = HBaseService.getRow(connection, StorageRingUtil.getObjTableName(bucketName), get);
        if (result.isEmpty()) {
            return;
        }

        long len = Bytes.toLong(result.getValue(StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_LEN_QUALIFIER));
        if (len > StorageRingUtil.FILE_STORE_THRESHOLD) {
            // 从HDFS删除
            String fileDir = StorageRingUtil.FILE_STORE_ROOT + "/" + bucketName + "/" + seqId;
            hdfsService.deleteFile(fileDir, name);
        }

        HBaseService.deleteRow(connection, StorageRingUtil.getObjTableName(bucketName), objKey);
    }

    private boolean isDirEmpty(String bucketName, String key) throws IOException {
        return listDir(bucketName, key, null, 2).getObjectSummaries().size() == 0;
    }

    private boolean dirExist(String bucketName, String key) {
        return HBaseService.existsRow(connection, StorageRingUtil.getDirTableName(bucketName), key);
    }

    private String getDirSeqId(String bucketName, String key) throws IOException {
        Result result = HBaseService.getRow(connection, StorageRingUtil.getDirTableName(bucketName), key);
        if (result.isEmpty()) {
            return null;
        }
        return Bytes.toString(result.getValue(StorageRingUtil.DIR_META_CF_BYTES, StorageRingUtil.DIR_SEQID_QUALIFIER));
    }

    private String putDir(String bucketName, String key) throws Exception {
        if (dirExist(bucketName, key)) {
            return null;
        }

        // 从zk获取锁
        InterProcessMutex lock = null;
        try {
            String lockKey = key.replace("/", "_");
            lock = new InterProcessMutex(zkClient, "/stroagering/" + bucketName + "/" + lockKey);
            lock.acquire();
            String dir1 = key.substring(0, key.lastIndexOf("/"));
            String name = dir1.substring(dir1.lastIndexOf("/"));

            if (name.length() > 0) {
                String parent = dir1.substring(0, dir1.lastIndexOf("/"));
                if (!dirExist(bucketName, parent)) {
                    this.putDir(bucketName, parent);
                }

                // 在父目录列族sub内，添加子项
                Put put = new Put(Bytes.toBytes(parent));
                put.addColumn(StorageRingUtil.DIR_SUBDIR_CF_BYTES, Bytes.toBytes(name), Bytes.toBytes("1"));
                HBaseService.putRow(connection, StorageRingUtil.getDirTableName(bucketName), put);
            }

            // 再将bucket添加到目录表
            String seqId = getDirSeqId(bucketName, key);
            String hash = seqId == null ? makeDirSeqId(bucketName) : seqId;
            Put dirPut = new Put(key.getBytes());
            dirPut.addColumn(StorageRingUtil.DIR_META_CF_BYTES, StorageRingUtil.DIR_SEQID_QUALIFIER, Bytes.toBytes(hash));
            HBaseService.putRow(connection, StorageRingUtil.getDirTableName(bucketName), dirPut);
            return hash;
        } finally {
            if (lock != null) {
                lock.release();
            }
        }
    }

    private String makeDirSeqId(String bucketName) throws IOException {
        long v = HBaseService.incrementColumnValue(connection, StorageRingUtil.BUCKET_DIR_SEQ_TABLE, bucketName, StorageRingUtil.BUCKET_DIR_SEQ_CF_BYTES, StorageRingUtil.BUCKET_DIR_SEQ_QUALIFIER, 1);

        return String.format("%da%d", v % 64, v);
    }

    private StorageRingObjectSummary dirObject2Summary(Result result, String bucketName, String dir) {

        StorageRingObjectSummary summary = new StorageRingObjectSummary();
        summary.setId(Bytes.toString(result.getRow()));
        summary.setAttrs(new HashMap<>(0));
        summary.setBucket(bucketName);
        summary.setLastModifyTime(result.rawCells()[0].getTimestamp());
        summary.setLength(0);
        summary.setMediaType("");
        if (dir.length() > 1) {
            summary.setName(dir.substring(dir.lastIndexOf("/") + 1));
        } else {
            summary.setName("");
        }
        return summary;

    }

    private StorageRingObjectSummary result2ObjectSummary(Result result, String bucketName, String dir) throws IOException {
        StorageRingObjectSummary summary = new StorageRingObjectSummary();
        long timeStamp = result.rawCells()[0].getTimestamp();
        summary.setLastModifyTime(timeStamp);

        String id = new String(result.getRow());
        summary.setId(id);

        String name = id.split("_", 2)[1];
        summary.setName(name);

        summary.setKey(dir + name);
        summary.setBucket(bucketName);
        summary.setMediaType(Bytes.toString(
                result.getValue(StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_MEDIA_TYPE_QUALIFIER)));

        summary.setLength(Bytes.toLong(
                result.getValue(StorageRingUtil.OBJ_META_CF_BYTES, StorageRingUtil.OBJ_LEN_QUALIFIER))
        );

        String p = Bytes.toString(result.getValue(StorageRingUtil.OBJ_META_CF_BYTES,
                StorageRingUtil.OBJ_PROPS_QUALIFIER));
        if (p != null) {
            summary.setAttrs(JsonUtil.fromJson(Map.class, p));
        }

        return summary;
    }
}

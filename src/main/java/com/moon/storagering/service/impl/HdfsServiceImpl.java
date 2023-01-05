package com.moon.storagering.service.impl;

import com.moon.storagering.config.StorageRingConfiguration;
import com.moon.storagering.exception.bussness.FileSystemException;
import com.moon.storagering.service.IHdfsService;
import org.apache.commons.io.FileExistsException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public class HdfsServiceImpl implements IHdfsService {

    private static Logger logger = Logger.getLogger(HdfsServiceImpl.class);

    private FileSystem fileSystem;

    private long defaultBlockSize = 128 * 1024 * 1024;
    private long initBlockSize = defaultBlockSize / 2;

    private int fileTransferBufferSize = 512 * 1024;

    public HdfsServiceImpl() throws Exception {
        // 读取hdfs相关配置
        StorageRingConfiguration srConfig = StorageRingConfiguration.getConfiguration();
        String confDir = srConfig.getString("hadoop.conf.dir");
        String hdfsUri = srConfig.getString("hadoop.uri");
        // 根据配置，获取一个fileSystem
        Configuration conf = new Configuration();
        conf.addResource(new Path(confDir + "hdfs-site.xml"));
        conf.addResource(new Path(confDir + "core-site.xml"));
        fileSystem = FileSystem.get(new URI(hdfsUri), conf);
    }

    @Override
    public void saveFile(String dir, String name, InputStream inputStream, long length, short relication) throws IOException {
        // 判断目录是否存在
        Path dirPath = new Path(dir);
        try {
            if (!fileSystem.exists(dirPath)) {
                boolean success = fileSystem.mkdirs(dirPath, FsPermission.getDirDefault());
                logger.info("create dis " + dirPath);
                if (!success) {
                    throw new FileSystemException(10001);
                }
            }
        } catch (FileExistsException ex) {
            ex.printStackTrace();
        }

        // 保存文件
        Path path = new Path(dir + "/" + name);
        long blockSize = length <= initBlockSize ? initBlockSize : defaultBlockSize;

        try (inputStream;
             FSDataOutputStream outputStream =
                     fileSystem.create(path, true, fileTransferBufferSize, relication, blockSize)) {
            fileSystem.setPermission(path, FsPermission.getFileDefault());
            byte[] buffer = new byte[fileTransferBufferSize];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        }
    }

    @Override
    public void deleteFile(String dir, String name) throws IOException {
        fileSystem.delete(new Path(dir + "/" + name), false);
    }

    @Override
    public InputStream openFile(String dir, String name) throws IOException {
        return fileSystem.open(new Path(dir + "/" + name));
    }

    @Override
    public void mkDir(String dir) throws IOException {
        fileSystem.mkdirs(new Path(dir));
    }

    @Override
    public void deleteDir(String dir) throws IOException {
        fileSystem.delete(new Path(dir), true);
    }
}

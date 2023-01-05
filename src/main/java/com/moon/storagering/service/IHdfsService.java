package com.moon.storagering.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public interface IHdfsService {

    void saveFile(String dir, String name, InputStream inputStream,
                  long length, short relication) throws IOException;

    void deleteFile(String dir, String name) throws IOException;

    InputStream openFile(String dir, String name) throws IOException;

    void mkDir(String dir) throws IOException;

    void deleteDir(String dir) throws IOException;
}

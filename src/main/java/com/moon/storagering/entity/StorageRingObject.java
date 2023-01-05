package com.moon.storagering.entity;


import com.moon.storagering.exception.bussness.FileSystemException;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Response;
import org.apache.log4j.Logger;
import org.mortbay.log.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Chanmoey
 * @date 2023年01月05日
 */
@Getter
@Setter
public class StorageRingObject implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(StorageRingObject.class);

    private ObjectMetaData metaData;

    private InputStream content;

    private Response response;

    public StorageRingObject() {

    }

    public StorageRingObject(Response response) {
        this.response = response;
    }

    @Override
    public void close() {
        try {
            if (content != null) {
                content.close();
            }
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(21001);
        }
    }
}

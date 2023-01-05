package com.moon.storagering.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Chanmoey
 * @date 2023年01月05日
 */
@Getter
@Setter
public class ObjectMetaData {

    private String bucket;

    /**
     * 全路径
     */
    private String key;

    private String mediaType;

    private long length;

    private long lastModifyTime;

    private Map<String, String> attrs;

    public String getContentEncoding() {
        return attrs != null ? attrs.get("content-encoding") : null;
    }
}

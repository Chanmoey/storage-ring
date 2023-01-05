package com.moon.storagering.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Chanmoey
 * @date 2023年01月05日
 */
@Getter
@Setter
public class StorageRingObjectSummary implements Comparable<StorageRingObjectSummary>, Serializable {

    private String id;

    private String key;

    private String name;

    private long length;

    private String mediaType;

    private long lastModifyTime;

    private String bucket;

    private Map<String, String> attrs;

    public String getContentEncoding() {
        return attrs != null ? attrs.get("content-encoding") : null;
    }

    @Override
    public int compareTo(StorageRingObjectSummary o) {
        return this.getKey().compareTo(o.getKey());
    }
}

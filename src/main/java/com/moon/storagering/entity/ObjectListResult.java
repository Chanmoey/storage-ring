package com.moon.storagering.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月05日
 */
@Getter
@Setter
public class ObjectListResult {

    private String bucket;

    private String maxKey;

    private String minKey;

    private String nextMarker;

    private int maxKeyNumber;

    private int objectCount;

    private String listId;

    private List<StorageRingObjectSummary> objectSummaries;

}

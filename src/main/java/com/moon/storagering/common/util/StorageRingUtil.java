package com.moon.storagering.common.util;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public class StorageRingUtil {

    private StorageRingUtil() {
    }

    public static final byte[][] OBJ_REGIONS = new byte[][]{
            Bytes.toBytes("1"),
            Bytes.toBytes("4"),
            Bytes.toBytes("7")
    };

    public static final String OBJ_TABLE_PREFIX = "storage_ring_obj_";
    public static final String DIR_TABLE_PREFIX = "storage_ring_dir_";

    public static final String DIR_META_CF = "cf";
    public static final byte[] DIR_META_CF_BYTES = DIR_META_CF.getBytes();
    public static final String DIR_SUBDIR_CF = "sub";
    public static final byte[] DIR_SUBDIR_CF_BYTES = DIR_SUBDIR_CF.getBytes();

    public static final String OBJ_CONT_CF = "c";
    public static final byte[] OBJ_CONT_CF_BYTES = OBJ_CONT_CF.getBytes();
    public static final String OBJ_META_CF = "cf";
    public static final byte[] OBJ_META_CF_BYTES = OBJ_META_CF.getBytes();

    public static final byte[] DIR_SEQID_QUALIFIER = "u".getBytes();
    public static final byte[] OBJ_CONT_QUALIFIER = "c".getBytes();
    public static final byte[] OBJ_LEN_QUALIFIER = "l".getBytes();
    public static final byte[] OBJ_PROPS_QUALIFIER = "p".getBytes();
    public static final byte[] OBJ_MEDIA_TYPE_QUALIFIER = "m".getBytes();

    public static final String FILE_STORE_ROOT = "/storage_ring";
    public static final int FILE_STORE_THRESHOLD = 20 * 1024 * 1024;
    public static final int OBJ_LIST_MAX_COUNT = 200;
    public static final String BUCKET_DIR_SEQ_TABLE = "storage_ring_dir_seq";
    public static final String BUCKET_DIR_SEQ_CF = "s";
    public static final byte[] BUCKET_DIR_SEQ_CF_BYTES = BUCKET_DIR_SEQ_CF.getBytes();
    public static final byte[] BUCKET_DIR_SEQ_QUALIFIER = "s".getBytes();

//    static final FilterList OBJ_META_SCAN_FILTER = new FilterList(Operator.MUST_PASS_ONE);

//    static {
//        try {
//            byte[][] qualifiers = new byte[][]{stroageringUtil.DIR_SEQID_QUALIFIER,
//                    stroageringUtil.OBJ_LEN_QUALIFIER,
//                    stroageringUtil.OBJ_MEDIATYPE_QUALIFIER};
//            for (byte[] b : qualifiers) {
//                Filter filter = new QualifierFilter(CompareOp.EQUAL,
//                        new BinaryComparator(b));
//                OBJ_META_SCAN_FILTER.addFilter(filter);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static String getDirTableName(String bucketName) {
        return DIR_TABLE_PREFIX + bucketName;
    }

    public static String getObjTableName(String bucketName) {
        return OBJ_TABLE_PREFIX + bucketName;
    }

    public static String[] getDirColumnFamily() {
        return new String[]{DIR_SUBDIR_CF, DIR_META_CF};
    }

    public static String[] getObjColumnFamily() {
        return new String[]{OBJ_META_CF, OBJ_CONT_CF};
    }
}

package com.moon.storagering.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public class CoreUtil {

    private CoreUtil() {
    }

    public static final String SYSTEM_USER = "SuperAdmin";

    public static String getMd5Password(String str) {
        String reStr = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            StringBuilder stringBuffer = new StringBuilder();
            for (byte b : bytes) {
                int bt = b & 0xff;
                if (bt < 16) {
                    stringBuffer.append(0);
                }
                stringBuffer.append(Integer.toHexString(bt));
            }
            reStr = stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return reStr;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

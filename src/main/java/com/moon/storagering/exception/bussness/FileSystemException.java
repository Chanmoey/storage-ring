package com.moon.storagering.exception.bussness;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public class FileSystemException extends BusinessException{

    public FileSystemException(int code) {
        this.code = code;
    }
}

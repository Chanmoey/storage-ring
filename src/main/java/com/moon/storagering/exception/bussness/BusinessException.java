package com.moon.storagering.exception.bussness;

import lombok.Getter;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Getter
public class BusinessException extends RuntimeException {
    protected int code;
    protected int httpStatusCode = 500;
}

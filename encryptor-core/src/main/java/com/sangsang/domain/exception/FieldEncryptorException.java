package com.sangsang.domain.exception;

/**
 * 自定义异常
 *
 * @author liutangqi
 * @date 2024/9/19 9:13
 */
public class FieldEncryptorException extends RuntimeException {

    public FieldEncryptorException(String message) {
        super(message);
    }
}

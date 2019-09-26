package com.cenzhipeng.redis.exception;

/**
 * throws when we decode the arg length with some errors
 */
public class ArgLengthException extends Exception {

    public ArgLengthException(String msg) {
        super(msg);
    }
}

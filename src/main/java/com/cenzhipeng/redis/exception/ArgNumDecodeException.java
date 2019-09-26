package com.cenzhipeng.redis.exception;

/**
 * throws when we decode the arg num with some errors
 */
public class ArgNumDecodeException extends Exception {

    public ArgNumDecodeException(String msg){
        super(msg);
    }
}

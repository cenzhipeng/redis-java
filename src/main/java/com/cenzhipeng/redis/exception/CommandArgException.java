package com.cenzhipeng.redis.exception;

/**
 * throws when the arg not match the command
 */
public class CommandArgException extends Exception {

    public CommandArgException(String msg) {
        super(msg);
    }
}

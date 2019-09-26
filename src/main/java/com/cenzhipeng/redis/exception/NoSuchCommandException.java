package com.cenzhipeng.redis.exception;

/**
 * throws when decode a command name that we not support
 */
public class NoSuchCommandException extends Exception {
    private String commandName;

    public NoSuchCommandException(String msg, String commandName) {
        super(msg);
        this.commandName = commandName;
    }

    public String commandName() {
        return commandName;
    }
}

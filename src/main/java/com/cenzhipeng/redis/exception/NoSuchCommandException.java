package com.cenzhipeng.redis.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * throws when decode a command name that we not support
 */
public class NoSuchCommandException extends Exception {
    private String commandName;
    private List<String> commandArgs = new ArrayList<>();

    public NoSuchCommandException(String msg, String commandName) {
        super(msg);
        this.commandName = commandName;
    }

    public void withArg(String arg) {
        commandArgs.add(arg);
    }

    public String commandName() {
        return commandName;
    }

    @Override
    public String toString() {
        String result = String.format("ERR unknown command `%s`, with args beginning with: ", commandName);
        StringBuilder args = new StringBuilder();
        for (String arg : commandArgs) {
            args.append("`").append(arg).append("`, ");
        }
        return result + args;
    }
}

package com.cenzhipeng.redis.command;

import com.cenzhipeng.redis.data.DataProvider;
import com.cenzhipeng.redis.exception.CommandArgException;
import com.cenzhipeng.redis.exception.NoSuchCommandException;
import com.cenzhipeng.redis.response.Response;

public interface Command {

    static Command valueOf(String commandName) throws NoSuchCommandException {
        //todo
        switch (commandName) {
            case SetCommand.NAME:
                return new SetCommand();
        }
        throw new NoSuchCommandException("no such command", commandName);
    }

    /**
     * execute the command
     * @return
     */
    Response execute();

    /**
     * set the next arg of this command
     * @param arg the next arg
     * @throws CommandArgException when the arg doesn't match the command
     */
    void withArg(String arg) throws CommandArgException;

    /**
     * is the command valid
     * @return true when valid
     */
    boolean isValid();

    void dataProvider(DataProvider dataProvider);
}

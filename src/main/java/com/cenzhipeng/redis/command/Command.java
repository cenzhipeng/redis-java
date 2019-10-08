package com.cenzhipeng.redis.command;

import com.cenzhipeng.redis.data.DataProvider;
import com.cenzhipeng.redis.exception.CommandArgException;
import com.cenzhipeng.redis.exception.NoSuchCommandException;
import com.cenzhipeng.redis.response.Response;

public interface Command {

    static Command valueOf(String commandName) throws NoSuchCommandException {
        //todo
        switch (commandName.toUpperCase()) {
            case SetCommand.NAME:
                return new SetCommand();
            case GetCommand.NAME:
                return new GetCommand();
        }
        throw new NoSuchCommandException("no such command", commandName);
    }

    /**
     * execute the command
     *
     * @return
     */
    Response execute();

    /**
     * set the next arg of this command
     *
     * @param arg the next arg
     */
    void withArg(String arg);

    /**
     * build the command
     * we should always build the command before we execute it
     *
     * @throws CommandArgException when command is not valid
     */
    void build() throws CommandArgException;

    void dataProvider(DataProvider dataProvider);
}

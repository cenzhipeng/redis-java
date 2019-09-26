package com.cenzhipeng.redis.command;

import com.cenzhipeng.redis.data.DataProvider;
import com.cenzhipeng.redis.exception.CommandArgException;
import com.cenzhipeng.redis.response.Response;

public class SetCommand implements Command {
    static final String NAME = "SET";

    private DataProvider dataProvider;

    @Override
    public Response execute() {
        //todo
        return null;
    }

    @Override
    public void withArg(String arg) throws CommandArgException {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void dataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}

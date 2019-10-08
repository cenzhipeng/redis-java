package com.cenzhipeng.redis.command;

import com.cenzhipeng.redis.data.DataProvider;
import com.cenzhipeng.redis.exception.CommandArgException;
import com.cenzhipeng.redis.response.Response;
import com.cenzhipeng.redis.response.StringResponse;

import java.util.ArrayList;
import java.util.List;

public class GetCommand implements Command {
    static final String NAME = "GET";
    private DataProvider dataProvider;
    private List<String> args = new ArrayList<>();
    private String key;

    @Override
    public Response execute() {
        String value = dataProvider.get(key);
        return new StringResponse(value);
    }

    @Override
    public void withArg(String arg) {
        args.add(arg);
    }

    @Override
    public void build() throws CommandArgException {
        argNumCheck();
        key = args.get(0);
    }

    private void argNumCheck() throws CommandArgException {
        if (args.size() != 1) {
            throw new CommandArgException("ERR wrong number of arguments for 'get' command");
        }
    }

    @Override
    public void dataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}

package com.cenzhipeng.redis.command;

import com.cenzhipeng.redis.data.DataProvider;
import com.cenzhipeng.redis.exception.CommandArgException;
import com.cenzhipeng.redis.response.OKResponse;
import com.cenzhipeng.redis.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SET key value [expiration EX seconds|PX milliseconds] [NX|XX]
 */
public class SetCommand implements Command {
    static final String NAME = "SET";
    private static final String EX_ARG = "EX";
    private static final String PX_ARG = "PX";
    private static final String NX_ARG = "NX";
    private static final String XX_ARG = "XX";

    private DataProvider dataProvider;
    private List<String> args = new ArrayList<>();
    // command detail with key, value, expireTime, setType
    private Detail detail;

    /**
     * normal means overwrite
     * nx means only set when key not exists
     * xx means only set when key exists
     */
    private enum SetType {
        NORMAL,
        NX,
        XX
    }

    private class Detail {
        private String key;
        private String value;
        private long expireTime;
        private TimeUnit timeUnit;
        private SetType setType;
    }


    @Override
    public Response execute() {
        //todo, complete the setType and expire time
        dataProvider.set(detail.key, detail.value);
        return OKResponse.INSTANCE;
    }

    @Override
    public void withArg(String arg) {
        args.add(arg);
    }

    @Override
    public void build() throws CommandArgException {
        argNumCheck();
        parseDetail();
    }

    private void parseDetail() throws CommandArgException {
        detail = new Detail();
        detail.key = args.get(0);
        detail.value = args.get(1);
        int length = args.size();
        if (length > 2) {
            switch (args.get(2).toUpperCase()) {
                case EX_ARG:
                case PX_ARG:
                    parseExpireTime(2, false);
                    parseSetType(4);
                    break;
                case NX_ARG:
                case XX_ARG:
                    parseSetType(2);
                    parseExpireTime(3, true);
                    break;
                default:
                    throw new CommandArgException("ERR syntax error");
            }
        }
    }

    private void parseExpireTime(int expireIndex, boolean optional) throws CommandArgException {
        if (args.size() < expireIndex + 1) {
            if (optional) {
                return;
            }
            throw new CommandArgException("ERR syntax error");
        }
        //expire arg and the expire time must appear together
        if (args.size() < expireIndex + 2) {
            throw new CommandArgException("ERR syntax error");
        }
        String expireType = args.get(expireIndex);
        switch (expireType.toUpperCase()) {
            case EX_ARG:
                detail.timeUnit = TimeUnit.SECONDS;
                break;
            case PX_ARG:
                detail.timeUnit = TimeUnit.MILLISECONDS;
                break;
            default:
                throw new CommandArgException("ERR syntax error");
        }
        String timeStr = args.get(expireIndex + 1);
        try {
            Long time = Long.valueOf(timeStr);
            if (time <= 0) {
                throw new CommandArgException("ERR invalid expire time in set");
            }
            detail.expireTime = time;
        } catch (NumberFormatException e) {
            throw new CommandArgException("ERR value is not an integer or out of range");
        }
    }

    private void parseSetType(int setTypeIndex) throws CommandArgException {
        if (args.size() < setTypeIndex + 1) {
            detail.setType = SetType.NORMAL;
            return;
        }
        String setType = args.get(setTypeIndex);
        switch (setType.toUpperCase()) {
            case NX_ARG:
                detail.setType = SetType.NX;
                break;
            case XX_ARG:
                detail.setType = SetType.XX;
                break;
            default:
                throw new CommandArgException("ERR syntax error");
        }
    }

    private void argNumCheck() throws CommandArgException {
        if (args.size() > 5) {
            throw new CommandArgException("ERR syntax error");
        }
        if (args.size() < 2) {
            throw new CommandArgException("ERR wrong number of arguments for 'set' command");
        }
    }

    @Override
    public void dataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}

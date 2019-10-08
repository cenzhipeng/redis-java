package com.cenzhipeng.redis.response;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class OKResponse implements Response {
    public static OKResponse INSTANCE = new OKResponse();

    private OKResponse() {
    }

    @Override
    public void encode(ByteBuf byteBuf) {
        byteBuf.writeCharSequence("+OK\r\n", CharsetUtil.UTF_8);
    }
}

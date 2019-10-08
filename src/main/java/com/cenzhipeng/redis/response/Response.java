package com.cenzhipeng.redis.response;

import io.netty.buffer.ByteBuf;

public interface Response {
    public void encode(ByteBuf byteBuf);
}

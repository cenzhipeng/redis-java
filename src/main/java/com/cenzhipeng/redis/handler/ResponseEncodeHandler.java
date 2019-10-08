package com.cenzhipeng.redis.handler;

import com.cenzhipeng.redis.response.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class ResponseEncodeHandler extends MessageToByteEncoder<Response> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Response msg, ByteBuf out) throws Exception {
        msg.encode(out);
    }
}

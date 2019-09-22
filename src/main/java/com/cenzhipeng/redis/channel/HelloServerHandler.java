package com.cenzhipeng.redis.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class HelloServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        log.info("server receive message: [{}]",buf.toString(CharsetUtil.UTF_8));
        ByteBuf sendMsg = ctx.alloc().buffer();
        sendMsg.writeCharSequence("$13\r\nhello, world!\r\n",CharsetUtil.UTF_8);
        ctx.writeAndFlush(sendMsg);
    }
}

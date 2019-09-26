package com.cenzhipeng.redis.handler;

import com.cenzhipeng.redis.command.SetCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SetCommandHandler extends SimpleChannelInboundHandler<SetCommand> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SetCommand msg) throws Exception {
        msg.execute();
    }
}

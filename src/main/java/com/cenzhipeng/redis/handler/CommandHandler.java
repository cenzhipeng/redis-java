package com.cenzhipeng.redis.handler;

import com.cenzhipeng.redis.command.Command;
import com.cenzhipeng.redis.exception.CommandArgException;
import com.cenzhipeng.redis.response.Response;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Sharable
@Slf4j
public class CommandHandler extends SimpleChannelInboundHandler<Command> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        try {
            msg.build();
            Response response = msg.execute();
            ctx.channel().writeAndFlush(response);
        } catch (CommandArgException e) {
            ctx.fireChannelRead(e);
        }
    }
}

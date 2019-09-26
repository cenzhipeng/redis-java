package com.cenzhipeng.redis.handler;

import com.cenzhipeng.redis.command.Command;
import com.cenzhipeng.redis.data.Attributes;
import com.cenzhipeng.redis.exception.ArgLengthException;
import com.cenzhipeng.redis.exception.ArgNumDecodeException;
import com.cenzhipeng.redis.exception.CommandArgException;
import com.cenzhipeng.redis.exception.NoSuchCommandException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * this is a stateful handler, the state loop as following:
 * 1.receive the arg nums which start with *[arg_num]
 * 2.receive the arg byte length which start with $[arg_length]
 * 3.receive the arg command which byte length is exactly equals to [arg_length]
 * 4.loop step 2 and 3 till exactly [arg_num] times
 * 5.loop step 1 to 4
 */
@Slf4j
public class RequestStatefulHandler extends ByteToMessageDecoder {
    // a state in decode arg num
    private static final int DECODE_ARG_NUM = 0;
    // a state in decode arg length
    private static final int DECODE_ARG_LENGTH = 1;
    // a state in decode arg data
    private static final int DECODE_ARG_DATA = 2;

    // current state
    private int state = DECODE_ARG_NUM;
    // true when we decode the first arg data, which is a command name
    private boolean decodeCommand;
    // the left arg num of this command decoding
    private int argNum;
    // the next arg's data length(in bytes)
    private int argLength;
    // the netx command
    private Command command;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        switch (state) {
            case DECODE_ARG_NUM: {
                try {
                    readArgNum(in);
                } catch (ArgNumDecodeException e) {
                    log.error("error occurs when decoding the arg num", e);
                    ctx.channel().close();
                }
                return;
            }
            case DECODE_ARG_LENGTH: {
                try {
                    readArgLength(in);
                } catch (ArgNumDecodeException e) {
                    log.error("error occurs when decoding the arg length", e);
                    ctx.channel().close();
                }
                return;
            }
            case DECODE_ARG_DATA: {
                try {
                    readArgData(ctx, in, out);
                } catch (ArgLengthException e) {
                    log.error("error occurs when decoding the arg data", e);
                    ctx.channel().close();
                } catch (NoSuchCommandException e) {
                    log.error("not support the command:[{}]", e.commandName(), e);
                    ctx.channel().close();
                } catch (CommandArgException e) {
                    log.error("error occurs when decoding the command", e);
                    ctx.channel().close();
                }
            }
        }
    }


    private void readArgNum(ByteBuf in) throws ArgNumDecodeException {
        String argNumStrng = in.toString(CharsetUtil.UTF_8).intern();
        if (argNumStrng.charAt(0) != '*') {
            throw new ArgNumDecodeException("the arg num field is not start with '*'");
        }
        try {
            argNum = Integer.valueOf(argNumStrng.substring(1));
            if (argNum < 1) {
                throw new ArgNumDecodeException("the arg num is smaller than 1");
            }
            state = DECODE_ARG_LENGTH;
            decodeCommand = true;
        } catch (NumberFormatException e) {
            throw new ArgNumDecodeException("the arg num field is not a regular number");
        }
    }

    private void readArgLength(ByteBuf in) throws ArgNumDecodeException {
        String argNumStrng = in.toString(CharsetUtil.UTF_8).intern();
        if (argNumStrng.charAt(0) != '$') {
            throw new ArgNumDecodeException("the arg length field is not start with '$'");
        }
        try {
            argLength = Integer.valueOf(argNumStrng.substring(1));
            if (argLength < 0) {
                throw new ArgNumDecodeException("the arg length is smaller than 0");
            }
            state = DECODE_ARG_DATA;
        } catch (NumberFormatException e) {
            throw new ArgNumDecodeException("the arg length field is not a regular number");
        }
    }

    private void readArgData(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws ArgLengthException,
            NoSuchCommandException, CommandArgException {
        if (in.readableBytes() != argLength) {
            throw new ArgLengthException("the arg length decoded error, this is not a correct data packet");
        }
        argNum--;
        byte[] buf = new byte[argLength];
        in.readBytes(buf);
        String arg = new String(buf, CharsetUtil.UTF_8);
        if (decodeCommand) {
            command = Command.valueOf(arg);
            command.dataProvider(ctx.channel().attr(Attributes.DATA).get());
            decodeCommand = false;
        } else {
            command.withArg(arg);
        }
        if (argNum == 0) {
            if (command.isValid()) {
                out.add(command);
                state = DECODE_ARG_NUM;
            } else {
                throw new CommandArgException("command is not valid");
            }
        } else {
            state = DECODE_ARG_LENGTH;
        }
    }
}

package com.cenzhipeng.redis.handler;

import com.cenzhipeng.redis.command.Command;
import com.cenzhipeng.redis.data.Attributes;
import com.cenzhipeng.redis.exception.NoSuchCommandException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RequestDecodeHandler extends ByteToMessageDecoder {
    private enum State {
        DECODE_ARG_NUM,
        DECODE_COMMAND_NAME_LENGTH,
        DECODE_COMMAND_NAME,
        DECODE_ARG_LENGTH,
        DECODE_ARG_DATA
    }

    private State state = State.DECODE_ARG_NUM;
    private int argNum;
    // the byte num of next arg
    private int argLength;
    // the netx command
    private Command command;
    // when invoke a unknown command, this will be set
    private NoSuchCommandException commandException;
    // if we can decode a complete line in this loop
    private boolean ready;
    // the packet has an error, close the channel
    private boolean packetError;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ready = true;
        while (ready && !packetError) {
            switch (state) {
                case DECODE_ARG_NUM:
                    readArgNum(ctx, in);
                    break;
                case DECODE_COMMAND_NAME_LENGTH:
                    readCommandNameLength(ctx, in);
                    break;
                case DECODE_COMMAND_NAME:
                    readCommandName(ctx, in);
                    break;
                case DECODE_ARG_LENGTH:
                    readArgLength(ctx, in);
                    break;
                case DECODE_ARG_DATA:
                    readArgData(ctx, in, out);
            }
        }
    }

    private void readArgNum(ChannelHandlerContext ctx, ByteBuf in) {
        int readerIndex = in.readerIndex();
        int nIndex = in.indexOf(readerIndex, in.writerIndex(), (byte) '\n');
        int rIndex = nIndex - 1;
        if (rIndex <= readerIndex) {
            ready = false;
            return;
        }
        if ((in.getByte(rIndex)) != '\r') {
            log.error("the arg num field is not end with [CR LF], remote is:[{}]", ctx.channel().remoteAddress());
            ctx.channel().close();
            packetError = true;
            return;
        }
        byte[] dst = new byte[rIndex - readerIndex];
        in.readBytes(dst);
        in.skipBytes(2);
        String argNumString = new String(dst, 0, dst.length, CharsetUtil.UTF_8);
        if (argNumString.charAt(0) != '*') {
            log.error("the arg num field is not start with [*], remote is:[{}]", ctx.channel().remoteAddress());
            ctx.channel().close();
            packetError = true;
            return;
        }
        try {
            argNum = Integer.valueOf(argNumString.substring(1));
            if (argNum < 1) {
                log.error("the arg num:[{}] is smaller than [1], remote is:[{}]", argNum, ctx.channel().remoteAddress());
                ctx.channel().close();
                packetError = true;
                return;
            }
            state = State.DECODE_COMMAND_NAME_LENGTH;
        } catch (NumberFormatException e) {
            log.error("the arg num field is not a regular number, remote is:[{}]", ctx.channel().remoteAddress());
            ctx.channel().close();
            packetError = true;
        }
    }

    private void readCommandNameLength(ChannelHandlerContext ctx, ByteBuf in) {
        readArgLength(ctx, in);
    }

    private void readCommandName(ChannelHandlerContext ctx, ByteBuf in) {
        String arg = decodeArg(ctx, in);
        if (arg == null) {
            return;
        }
        try {
            command = Command.valueOf(arg);
            command.dataProvider(ctx.channel().attr(Attributes.DATA).get());
        } catch (NoSuchCommandException e) {
            commandException = e;
        } finally {
            state = State.DECODE_ARG_LENGTH;
        }
    }

    private void readArgData(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        String arg = decodeArg(ctx, in);
        if (arg == null) {
            return;
        }
        if (commandException != null) {
            commandException.withArg(arg);
        } else {
            command.withArg(arg);
        }
        if (argNum == 0) {
            if (commandException == null) {
                out.add(command);
            } else {
                out.add(commandException);
            }
            state = State.DECODE_ARG_NUM;
            commandException = null;
            command = null;
        } else {
            state = State.DECODE_ARG_LENGTH;
        }
    }

    private String decodeArg(ChannelHandlerContext ctx, ByteBuf in) {
        // +2 means CR and LF
        if (in.readableBytes() < argLength + 2) {
            ready = false;
            return null;
        }
        int readerIndex = in.readerIndex();
        if (in.getByte(readerIndex + argLength) != '\r' || in.getByte(readerIndex + argLength + 1) != '\n') {
            log.error("the arg data field is not end with [CR LF], remote is:[{}]", ctx.channel().remoteAddress());
            ctx.channel().close();
            packetError = true;
            return null;
        }
        argNum--;
        byte[] dst = new byte[argLength];
        in.readBytes(dst);
        in.skipBytes(2);
        return new String(dst, CharsetUtil.UTF_8);
    }

    private void readArgLength(ChannelHandlerContext ctx, ByteBuf in) {
        int readerIndex = in.readerIndex();
        int nIndex = in.indexOf(readerIndex, in.writerIndex(), (byte) '\n');
        int rIndex = nIndex - 1;
        if (rIndex <= readerIndex) {
            ready = false;
            return;
        }
        if ((in.getByte(rIndex)) != '\r') {
            log.error("the arg length field is not end with [CR LF], remote is:[{}]", ctx.channel().remoteAddress());
            ctx.channel().close();
            packetError = true;
            return;
        }
        byte[] dst = new byte[rIndex - readerIndex];
        in.readBytes(dst);
        in.skipBytes(2);
        String argLengthStrng = new String(dst, 0, dst.length, CharsetUtil.UTF_8);
        if (argLengthStrng.charAt(0) != '$') {
            log.error("the arg length field is not start with [$], remote is:[{}]", ctx.channel().remoteAddress());
            ctx.channel().close();
            packetError = true;
            return;
        }
        try {
            argLength = Integer.valueOf(argLengthStrng.substring(1));
            if (argLength < 0) {
                log.error("the arg length field is smaller than 0, remote is:[{}]", ctx.channel().remoteAddress());
                ctx.channel().close();
                packetError = true;
                return;
            }
            // it will be 2 condition with DECODE_COMMAND_NAME_LENGTH or DECODE_ARG_LENGTH
            if (state == State.DECODE_COMMAND_NAME_LENGTH) {
                state = State.DECODE_COMMAND_NAME;
            } else {
                state = State.DECODE_ARG_DATA;
            }
        } catch (NumberFormatException e) {
            log.error("the arg length field is not a regular number, remote is:[{}]", ctx.channel().remoteAddress());
            ctx.channel().close();
            packetError = true;
        }
    }
}

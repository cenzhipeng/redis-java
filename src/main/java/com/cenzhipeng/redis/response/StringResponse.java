package com.cenzhipeng.redis.response;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class StringResponse implements Response {
    private String msg;

    public StringResponse(String msg) {
        this.msg = msg;
    }

    @Override
    public void encode(ByteBuf byteBuf) {
        if (msg == null) {
            byteBuf.writeCharSequence("$-1\r\n", CharsetUtil.UTF_8);
        } else {
            byte[] bytes = msg.getBytes(CharsetUtil.UTF_8);
            byteBuf.writeByte('$');
            byteBuf.writeCharSequence(String.valueOf(bytes.length), CharsetUtil.UTF_8);
            byteBuf.writeCharSequence("\r\n", CharsetUtil.UTF_8);
            byteBuf.writeBytes(bytes);
            byteBuf.writeCharSequence("\r\n", CharsetUtil.UTF_8);
        }
    }
}

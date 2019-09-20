package com.cenzhipeng.redis;

public class RedisServer {
    private int port = 6379;

    public void start() {
        throw new UnsupportedOperationException("unimplemented yet");
    }

    public void shutDown() {
        throw new UnsupportedOperationException("unimplemented yet");
    }

    public static RedisServerBuilder builder() {
        return new RedisServerBuilder();
    }

    public static class RedisServerBuilder {

        public RedisServer build() {
            throw new UnsupportedOperationException("unimplemented yet");
        }
    }
}

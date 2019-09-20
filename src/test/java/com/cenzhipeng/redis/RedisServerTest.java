package com.cenzhipeng.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class RedisServerTest {
    private static RedisClient redisClient;
    private static RedisServer redisServer;

    @BeforeClass
    public static void initClient() {
        // start the embed redis server
        redisServer = RedisServer.builder().build();
        redisServer.start();
        RedisURI uri = RedisURI.builder()
                .withHost("localhost")
                .withPassword("123456")
                .withPort(6379)
                .withDatabase(0)
                .build();
        redisClient = RedisClient.create(uri);
    }

    @AfterClass
    public static void releaseClient(){
        redisClient.shutdown();
        redisServer.shutDown();
    }

}
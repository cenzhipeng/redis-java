package com.cenzhipeng.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class RedisServerTest {
    private static RedisClient redisClient;
    private static RedisServer redisServer;

    @BeforeClass
    public static void initClient() {
        // start the embed redis server
//        redisServer = RedisServer.builder().build();
//        redisServer.start();
        RedisURI uri = RedisURI.builder()
                .withHost("redis")
//                .withPassword("123456")
                .withPort(63791)
                .withDatabase(0)
                .build();
        redisClient = RedisClient.create(uri);
    }

    @AfterClass
    public static void releaseClient(){
        redisClient.shutdown();
//        redisServer.shutDown();
    }

    @Test
    public void testInfo(){
        log.info(redisClient.connect().sync().info());
    }

}
package com.cenzhipeng.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

@Slf4j
public class RedisServerTest {
    private static RedisClient redisClient;
    private static RedisServer redisServer;

    @BeforeClass
    public static void initClient() throws InterruptedException {
        // start the embed redis server
        redisServer = RedisServer.builder().build();
        redisServer.start();

        RedisURI embedUri = RedisURI.builder()
                .withHost("localhost")
                .withPort(6379)
                .withDatabase(0)
                .build();
        redisClient = RedisClient.create(embedUri);
    }

    @AfterClass
    public static void releaseClient() throws InterruptedException {
        redisClient.shutdown();
        redisServer.shutDown();
    }


    @Test
    public void testSet() throws UnsupportedEncodingException, InterruptedException {
//        Thread.sleep(50000);
        String info = redisClient.connect().sync().get("xxx");
        assertThat(info).isEqualTo("hello, world!");
    }

}
package com.cenzhipeng.redis;

import com.cenzhipeng.redis.handler.TestServerHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * this class is used for testing what msg does server receive
 */
@Slf4j
public class RedisServerReceiveTest {
    private static RedisClient redisClient;
    private static RedisServer redisServer;

    @BeforeClass
    public static void initClient() throws InterruptedException {
        // start the embed redis server
        redisServer = RedisServer.builder().build();
//        redisServer.handlers(new TestServerHandler()).start();
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

    /**
     * test for pipeline request
     */
    @Test
    public void setAndGet() {
        RedisCommands<String, String> client = redisClient.connect().sync();
        String ok = client.set("aaa", "bbb");
        String bbb = client.get("aaa");
        assertThat(ok).isEqualTo("OK");
        assertThat(bbb).isEqualTo("bbb");
    }

}
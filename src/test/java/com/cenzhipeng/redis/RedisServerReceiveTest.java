package com.cenzhipeng.redis;

import com.cenzhipeng.redis.handler.TestServerHandler;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        redisServer.handlers(new TestServerHandler()).start();

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
    public void testBulk() {
        RedisAsyncCommands<String, String> commands = redisClient.connect().async();
        commands.setAutoFlushCommands(false);
        List<RedisFuture<?>> futures = new ArrayList<>();
        futures.add(commands.get("key1"));
        futures.add(commands.get("key2"));
        futures.add(commands.info());
        futures.add(commands.ping());
        commands.flushCommands();
        LettuceFutures.awaitAll(5, TimeUnit.SECONDS,futures.toArray(new RedisFuture[futures.size()]));
    }

}
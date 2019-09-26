package com.cenzhipeng.redis;

import com.cenzhipeng.redis.data.Attributes;
import com.cenzhipeng.redis.data.DataProvider;
import com.cenzhipeng.redis.handler.RequestStatefulHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class RedisServer {
    private static final int MAX_DATABASE_NUM = 1024;
    private volatile Channel channel;

    private int port;
    private int databaseNum;
    private String password;
    private ChannelHandler[] handlers;
    private volatile DataProvider dataProvider = new DataProvider();


    public int port() {
        return port;
    }

    public int databaseNum() {
        return databaseNum;
    }

    private RedisServer() {

    }

    /**
     * for test
     *
     * @param handlers
     */
    RedisServer handlers(ChannelHandler... handlers) {
        this.handlers = handlers;
        return this;
    }

    public void start() throws InterruptedException {
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childHandler(new ChannelInitializer<NioSocketChannel>() {
                            protected void initChannel(NioSocketChannel ch) {
                                // todo; should insert  serviceHandlers here
//                                ch.pipeline().addLast(new TestServerHandler());
                                ch.attr(Attributes.DATA).set(dataProvider);
                                if (handlers != null) {
                                    Arrays.stream(handlers).forEach(handler -> {
                                        ch.pipeline().addLast(handler);
                                    });
                                } else {
                                    ch.pipeline()
                                            .addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE))
                                            .addLast(new RequestStatefulHandler());

                                }
                            }
                        });
                ChannelFuture f = bind(serverBootstrap, port);
                channel = f.channel();
                log.info("redis server started in port: [{}] database num: [{}]", port, databaseNum);
                countDownLatch.countDown();
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // make sure it countDown to 0
                countDownLatch.countDown();
                try {
                    bossGroup.shutdownGracefully().sync();
                    workerGroup.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // wait to confirm the server started or it has closed
        countDownLatch.await();
    }

    private ChannelFuture bind(ServerBootstrap serverBootstrap, int port) {
        try {
            ChannelFuture future = serverBootstrap.bind("0.0.0.0", port).sync();
            log.info("server port: [{}] bind success", port);
            return future;
        } catch (Throwable e) {
            log.error("server port: [{}] bind fail, the detail is：", port, e);
            log.info("try to bind server port: [{}]", port + 1);
            this.port = port + 1;
            return bind(serverBootstrap, this.port);
        }
    }

    public void shutDown() throws InterruptedException {
        if (channel == null) {
            log.warn("invoke server shutdown but server has not started, please isValid your code");
            return;
        }
        channel.close().sync();
    }

    public static RedisServerBuilder builder() {
        return new RedisServerBuilder();
    }

    public static class RedisServerBuilder {
        private int port = 6379;
        private int databaseNum = 16;
        private String password;

        public RedisServerBuilder port(int port) {
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("bind port should be in 1 to 65535");
            }
            this.port = port;
            return this;
        }

        public RedisServerBuilder databaseNum(int databaseNum) {
            if (databaseNum < 1 || databaseNum > MAX_DATABASE_NUM) {
                throw new IllegalArgumentException("database num should be in 1 to " + MAX_DATABASE_NUM);
            }
            this.databaseNum = databaseNum;
            return this;
        }

        public RedisServerBuilder password(String password) {
            this.password = password;
            return this;
        }

        public RedisServer build() {
            RedisServer redisServer = new RedisServer();

            redisServer.port = port;
            redisServer.databaseNum = databaseNum;
            redisServer.password = password;

            return redisServer;
        }
    }
}

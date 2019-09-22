package com.cenzhipeng.redis;

import com.cenzhipeng.redis.channel.HelloServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class RedisServer {
    private static final int MAX_DATABASE_NUM = 1024;
    private volatile Channel channel;

    private int port;
    private int databaseNum;
    private String password;


    public int port() {
        return port;
    }

    public int databaseNum() {
        return databaseNum;
    }

    private RedisServer() {

    }

    public void start() throws InterruptedException {
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                try {
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                                protected void initChannel(NioSocketChannel ch) {
                                    // todo; should insert  serviceHandlers here
                                    ch.pipeline().addLast(new HelloServerHandler());
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
            }
        }.start();
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
            log.warn("invoke server shutdown but server has not started, please check your code");
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

        public void withPort(int port) {
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("bind port should be in 1 to 65535");
            }
            this.port = port;
        }

        public void withDatabaseNum(int databaseNum) {
            if (databaseNum < 1 || databaseNum > MAX_DATABASE_NUM) {
                throw new IllegalArgumentException("database num should be in 1 to " + MAX_DATABASE_NUM);
            }
            this.databaseNum = databaseNum;
        }

        public void withPassword(String password) {
            this.password = password;
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

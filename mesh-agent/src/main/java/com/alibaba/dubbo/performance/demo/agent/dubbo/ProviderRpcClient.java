package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:20
 */
public class ProviderRpcClient {

    private Bootstrap bootstrap;

    private Endpoint endpoint;

    public ProviderRpcClient() {
        this.endpoint = new Endpoint("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")), 0);

        this.bootstrap = new Bootstrap()
                .group(new EpollEventLoopGroup())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class);
    }

    public ChannelFuture connect(Channel sourceChannel) throws InterruptedException {

        bootstrap.handler(new ProviderRpcHandler(sourceChannel));

        return bootstrap.connect(endpoint.getHost(), endpoint.getPort());
    }

}

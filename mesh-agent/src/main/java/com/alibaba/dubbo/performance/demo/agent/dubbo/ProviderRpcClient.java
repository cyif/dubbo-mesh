package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.AgentConstant;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:20
 */
public class ProviderRpcClient {

    private Logger logger = LoggerFactory.getLogger(ProviderRpcClient.class);

    private Bootstrap bootstrap;

    private Endpoint endpoint;

    private Channel channel;

    private final Object lock = new Object();

    public ProviderRpcClient() {
        this.endpoint = new Endpoint("127.0.0.1", AgentConstant.DUBBO_PORT, 0);

        this.bootstrap = new Bootstrap()
                .group(new EpollEventLoopGroup())
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(EpollSocketChannel.class);
    }

    public void setHandler(ChannelHandler handler) {
        bootstrap.handler(handler);
    }

    public Channel connect() throws InterruptedException {

        if (null == channel) {
            synchronized (lock) {
                if (null == channel) {
                    channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
                }
            }
        }

        return channel;
    }

}

package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.dubbo.loadbalance.RoundRobinLoadBalance;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:52
 */
public class ConsumerRpcClient{

    private List<Endpoint> endpoints;

    private Map<Endpoint, Channel> channelMap;

    private List<Channel> channels;

    private Bootstrap bootstrap;

    private IRegistry registry;

    private LoadBalance loadBalance = new RoundRobinLoadBalance();

    private final Object lock = new Object();

    public ConsumerRpcClient(IRegistry registry, EventLoopGroup worker) {
        this.registry = registry;
        this.bootstrap = new Bootstrap()
                .group(new EpollEventLoopGroup())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(EpollSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(worker,
                                new DubboRpcEncoder(),
                                new DubboRpcDecoder(),
                                new ConsumerRpcHandler());
                    }
                });
    }

    public Channel getChannel() throws Exception {
        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                    channelMap = new HashMap<>();
                    for (Endpoint endpoint : endpoints) {
                        Channel channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
                        channelMap.put(endpoint, channel);
                    }
                }
            }
        }

        Channel channel = channelMap.get(loadBalance.select(endpoints));
        return channel;
    }
}

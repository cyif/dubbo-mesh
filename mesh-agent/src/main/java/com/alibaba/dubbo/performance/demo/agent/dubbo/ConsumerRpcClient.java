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
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


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

    private Bootstrap bootstrap;

    private IRegistry registry;

    private LoadBalance loadBalance = new RoundRobinLoadBalance();

    private final Object lock = new Object();

    public ConsumerRpcClient(IRegistry registry) {
        this.registry = registry;
        this.bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup(16))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DubboRpcEncoder());
                        pipeline.addLast(new DubboRpcDecoder());
                        pipeline.addLast(new ConsumerRpcHandler());
                    }
                });
    }

    public Channel getChannel() throws Exception {
        if (null == channelMap) {
            synchronized (lock) {
                if (null == channelMap) {
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

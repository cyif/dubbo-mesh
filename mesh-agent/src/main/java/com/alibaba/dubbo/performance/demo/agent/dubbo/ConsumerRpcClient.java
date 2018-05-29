package com.alibaba.dubbo.performance.demo.agent.dubbo;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:52
 */
public class ConsumerRpcClient{

    private List<Channel> channels;

    private Bootstrap bootstrap;

    private IRegistry registry;

    private Random random = new Random();

    private List<Endpoint> endpoints;

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
        if (null == endpoints) {
            synchronized (this) {
                if (null == endpoints) {
                    List<Endpoint> endpointList = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                    List<Channel> channelList = new ArrayList<>();
                    System.out.println("Find endpoints " + endpointList.size());
                    for (Endpoint endpoint : endpointList) {
                        Channel channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
                        channelList.add(channel);
                    }
                    this.channels = channelList;
                    this.endpoints = endpointList;
                }
            }
        }

        List<Channel> channelList = this.channels;
        Channel channel = channelList.get(random.nextInt(channelList.size()));
        if (channel == null) {
            throw new Exception("channel is null");
        }
        return channel;
    }
}

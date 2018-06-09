package com.alibaba.dubbo.performance.demo.mesh.rpc;

import com.alibaba.dubbo.performance.demo.mesh.agent.model.Agent;
import com.alibaba.dubbo.performance.demo.mesh.rpc.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.mesh.rpc.loadbalance.RoundRobinLoadBalance;
import com.alibaba.dubbo.performance.demo.mesh.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.mesh.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

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

    private Map<Endpoint, Channel> channelMap;

    private Bootstrap bootstrap;

    private IRegistry registry;

    private LoadBalance loadBalance;

    private final Object lock = new Object();

    public ConsumerRpcClient(IRegistry registry) {
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
                        ch.pipeline().addLast(
//                                new DubboRpcEncoder(),
//                                new DubboRpcDecoder(),
                                // decoded
                                new ProtobufVarint32FrameDecoder(),
                                new ProtobufDecoder(Agent.AgentReponse.getDefaultInstance()),
                                // encoded
                                new ProtobufVarint32LengthFieldPrepender(),
                                new ProtobufEncoder(),
                                new ConsumerRpcHandler());
                    }
                });
    }

    public Channel getChannel() throws Exception {
        if (null == loadBalance) {
            synchronized (lock) {
                if (null == loadBalance) {
                    List<Endpoint> endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                    loadBalance = new RoundRobinLoadBalance(endpoints);
                    channelMap = new HashMap<>();
                    for (Endpoint endpoint : endpoints) {
                        Channel channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
                        channelMap.put(endpoint, channel);
                    }
                }
            }
        }

        Channel channel = channelMap.get(loadBalance.select());
        return channel;
    }
}

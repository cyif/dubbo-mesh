package com.alibaba.dubbo.performance.demo.agent.rpc;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.dubbo.performance.demo.agent.rpc.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.rpc.loadbalance.RoundRobinLoadBalance;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:52
 */
public class ConsumerRpcClient{

    private IRegistry registry;

    private LoadBalance loadBalance = null;

    private Map<Endpoint, Channel> channelMap = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    public ConsumerRpcClient(IRegistry registry) {
        this.registry = registry;
    }

    public Channel getChannel(EventLoop eventLoop) throws Exception {

        if (null == loadBalance) {
            synchronized (lock) {
                if (null == loadBalance) {
                    List<Endpoint> endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                    loadBalance = new RoundRobinLoadBalance(endpoints);
                    channelMap = new HashMap<>();
//                    for (Endpoint endpoint : endpoints) {
//                        Channel channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
//                        channelMap.put(endpoint, channel);
//                    }
                }
            }
        }

        Endpoint endpoint = loadBalance.select();
        if (!channelMap.containsKey(endpoint)) {
            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoop)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(EpollSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    // decoded
                                    new ProtobufVarint32FrameDecoder(),
                                    new ProtobufDecoder(Agent.AgentResponse.getDefaultInstance()),
                                    // encoded
                                    new ProtobufVarint32LengthFieldPrepender(),
                                    new ProtobufEncoder(),
                                    new ConsumerRpcHandler());
                        }
                    });
            Channel channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).channel();
            channelMap.put(endpoint, channel);
        }
        return channelMap.get(endpoint);
    }
}

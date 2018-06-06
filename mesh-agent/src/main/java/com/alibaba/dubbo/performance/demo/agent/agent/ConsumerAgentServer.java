package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.api.AgentServer;
import com.alibaba.dubbo.performance.demo.agent.dubbo.ConsumerRpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午2:46
 */
public class ConsumerAgentServer implements AgentServer {

    private ServerBootstrap bootstrap;

    private IRegistry registry;

    private int port;

    public static Map<Long, Channel> channelMap = new ConcurrentHashMap<>(10000);

    public ConsumerAgentServer(int port) {
        init();
        this.port = port;
    }

    private void init() {
        registry = new EtcdRegistry(AgentConstant.ETCD_URL);
        bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new EpollEventLoopGroup(2);
        EventLoopGroup worker = new EpollEventLoopGroup();
        ConsumerRpcClient client = new ConsumerRpcClient(registry);

        bootstrap.group(boss, worker)
                .channel(EpollServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpServerCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(65536));
                        ch.pipeline().addLast(new ConsumerAgentServerHandler(client));
                    }
                });
    }

    @Override
    public void run() {
        try {
            ChannelFuture future = bootstrap.bind(port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bootstrap.config().group().shutdownGracefully();
            bootstrap.config().childGroup().shutdownGracefully();
        }
    }

}

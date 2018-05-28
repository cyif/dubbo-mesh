package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.ProviderRpcClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:38
 */
public class ProviderAgentServerHandler extends ChannelInboundHandlerAdapter {

    private ProviderRpcClient client;
    private Channel targetChannel;

    public ProviderAgentServerHandler(ProviderRpcClient client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        targetChannel.writeAndFlush(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        client.connect(ctx.channel()).addListener(future -> {
            targetChannel = ((ChannelFuture) future).channel();
        });
    }
}

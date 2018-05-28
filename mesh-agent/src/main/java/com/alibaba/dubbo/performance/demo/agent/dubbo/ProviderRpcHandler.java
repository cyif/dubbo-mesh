package com.alibaba.dubbo.performance.demo.agent.dubbo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-24
 * Time: 下午10:00
 */
public class ProviderRpcHandler extends ChannelInboundHandlerAdapter {

    private Channel sourceChannel;

    ProviderRpcHandler(Channel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        sourceChannel.writeAndFlush(msg);
    }
}

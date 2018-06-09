package com.alibaba.dubbo.performance.demo.mesh.rpc;

import com.alibaba.dubbo.performance.demo.mesh.agent.model.Agent;
import com.alibaba.dubbo.performance.demo.mesh.rpc.model.RpcResponse;
import com.google.protobuf.ByteString;
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

    public ProviderRpcHandler(Channel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcResponse response = (RpcResponse) msg;

        Agent.AgentReponse agentReponse = Agent.AgentReponse.newBuilder()
                .setId(response.getRequestId())
                .setValueBytes(ByteString.copyFrom(response.getBytes()))
                .build();
        sourceChannel.writeAndFlush(agentReponse);
    }
}

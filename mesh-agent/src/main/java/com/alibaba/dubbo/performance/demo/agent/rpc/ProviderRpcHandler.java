package com.alibaba.dubbo.performance.demo.agent.rpc;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.RpcResponse;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-24
 * Time: 下午10:00
 */
public class ProviderRpcHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ProviderRpcHandler.class);

    private Channel sourceChannel;

    public ProviderRpcHandler(Channel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcResponse response = (RpcResponse) msg;

        Agent.AgentResponse agentResponse = Agent.AgentResponse.newBuilder()
                .setId(response.getRequestId())
                .setValueBytes(ByteString.copyFrom(response.getBytes()))
                .build();
        logger.info("Return value : " + agentResponse.getValue());
        sourceChannel.writeAndFlush(agentResponse);
    }
}

package com.alibaba.dubbo.performance.demo.agent.rpc;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import com.alibaba.dubbo.performance.demo.agent.server.ConsumerAgentServerHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-24
 * Time: 下午11:20
 */
public class ConsumerRpcHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ConsumerRpcHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Agent.AgentResponse response = (Agent.AgentResponse) msg;
        Promise<Agent.AgentResponse> promise = ConsumerAgentServerHandler.processingRpc.get().remove(response.getId());
        if (null == promise) {
            logger.error("Fail to get promise : " + response.getId());
        } else {
            promise.trySuccess(response);
        }
    }
}

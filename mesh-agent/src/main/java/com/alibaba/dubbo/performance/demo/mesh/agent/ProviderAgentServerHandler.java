package com.alibaba.dubbo.performance.demo.mesh.agent;

import com.alibaba.dubbo.performance.demo.mesh.agent.model.Agent;
import com.alibaba.dubbo.performance.demo.mesh.rpc.DubboRpcDecoder;
import com.alibaba.dubbo.performance.demo.mesh.rpc.DubboRpcEncoder;
import com.alibaba.dubbo.performance.demo.mesh.rpc.ProviderRpcClient;
import com.alibaba.dubbo.performance.demo.mesh.rpc.ProviderRpcHandler;
import com.alibaba.dubbo.performance.demo.mesh.rpc.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.mesh.rpc.model.Request;
import com.alibaba.dubbo.performance.demo.mesh.rpc.model.RpcInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:38
 */
public class ProviderAgentServerHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ProviderAgentServerHandler.class);

    private ProviderRpcClient client;

    private Channel targetChannel;

    public ProviderAgentServerHandler(ProviderRpcClient client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Agent.AgentRequest agentRequest = (Agent.AgentRequest) msg;

        logger.info("Id : " + agentRequest.getId() + " content : " + agentRequest.toString());
        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(agentRequest.getMethodName());
        invocation.setAttachment("path", agentRequest.getInterfaceName());
        invocation.setParameterTypes(agentRequest.getParameterTypesString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(agentRequest.getParameter(), writer);
        invocation.setArguments(out.toByteArray());

        Request request = new Request(agentRequest.getId());
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);
        targetChannel.writeAndFlush(request);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        client.setHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        new DubboRpcDecoder(),
                        new DubboRpcEncoder(),
                        new ProviderRpcHandler(ctx.channel())
                );
            }
        });
        targetChannel = client.connect();
    }
}

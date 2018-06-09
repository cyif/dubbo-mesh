package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.ConsumerRpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
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
 * Time: 下午3:49
 */
public class ConsumerAgentServerHandler extends ChannelInboundHandlerAdapter{

    private Logger logger = LoggerFactory.getLogger(ConsumerAgentServerHandler.class);

    private ConsumerRpcClient client;

    private Channel targetChannel;

    private long channelId;

    public ConsumerAgentServerHandler(ConsumerRpcClient client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
//            Map<String, String> pMap = parse((FullHttpRequest) msg);
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.content().toString(), false);

            RpcInvocation invocation = new RpcInvocation();
            invocation.setMethodName(decoder.parameters().get("method").get(0));
            invocation.setAttachment("path", decoder.parameters().get("interface").get(0));
            invocation.setParameterTypes(decoder.parameters().get("parameterTypesString").get(0));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
            JsonUtils.writeObject(decoder.parameters().get("parameter").get(0), writer);
            invocation.setArguments(out.toByteArray());

            long id = (channelId << 30) + IdGenerator.getInstance().getRequestId();
            Request request = new Request(id);
            request.setVersion("2.0.0");
            request.setTwoWay(true);
            request.setData(invocation);
            targetChannel.writeAndFlush(request);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelId = IdGenerator.getInstance().getChannelId();
        ConsumerAgentServer.channelMap.put(channelId, ctx.channel());
        targetChannel = client.getChannel();
    }


}

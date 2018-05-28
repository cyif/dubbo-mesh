package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.ConsumerAgentServer;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-24
 * Time: 下午11:20
 */
public class ConsumerRpcHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcResponse response = (RpcResponse) msg;
        Channel channel = ConsumerAgentServer.channelMap.get(Long.valueOf(response.getRequestId()));
        if (channel == null) {
            throw new Exception("request channel is null");
        }

        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(response.getBytes()));
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
        resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        channel.writeAndFlush(resp);
    }
}

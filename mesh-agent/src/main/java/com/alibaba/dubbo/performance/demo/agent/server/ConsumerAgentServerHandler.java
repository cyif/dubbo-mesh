package com.alibaba.dubbo.performance.demo.agent.server;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import com.alibaba.dubbo.performance.demo.agent.rpc.ConsumerRpcClient;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.AgentPromise;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.AgentRequestHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    private int channelId;

    private Channel targetChannel;

    public ConsumerAgentServerHandler(ConsumerRpcClient client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            Map<String, String> pMap = parse((FullHttpRequest) msg);

            long id = channelId << 30 + IdGenerator.getInstance().getRequestId();

            Agent.AgentRequest request = Agent.AgentRequest.newBuilder()
                    .setId(id)
                    .setMethodName(pMap.get("method"))
                    .setInterfaceName(pMap.get("interface"))
                    .setParameterTypesString(pMap.get("parameterTypesString"))
                    .setParameter(pMap.get("parameter")).build();

//            AgentRequestHolder.put(request.getId(), new AgentPromise(ctx));
            targetChannel.writeAndFlush(request);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelId = IdGenerator.getInstance().getChannelId();
        ConsumerAgentServer.channels.put(channelId, ctx.channel());
        targetChannel = client.getChannel(ctx.channel().eventLoop());
    }

    private Map<String, String> parse(FullHttpRequest fullReq) throws IOException {
        HttpMethod method = fullReq.method();

        Map<String, String> parmMap = new HashMap<>();

        if (HttpMethod.GET == method) {
            logger.info("Http Get Request");
        } else if (HttpMethod.POST == method) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullReq);
            decoder.offer(fullReq);

            List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

            for (InterfaceHttpData parm : parmList) {

                Attribute data = (Attribute) parm;
                parmMap.put(data.getName(), data.getValue());
            }
        } else {
            logger.info("Unsupported method : ", method);
        }
        fullReq.release();
        return parmMap;
    }

}

package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.ConsumerRpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:49
 */
public class ConsumerAgentServerHandler extends ChannelInboundHandlerAdapter{

    private Logger logger = LoggerFactory.getLogger(ConsumerAgentServerHandler.class);

    private static AtomicLong requestId = new AtomicLong();

    private ConsumerRpcClient client;

    private Channel targetChannel;

    public ConsumerAgentServerHandler(ConsumerRpcClient client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Long id = requestId.incrementAndGet();

        ConsumerAgentServer.channelMap.put(id, ctx.channel());
        logger.info("request id : ", id);
        if (msg instanceof FullHttpRequest) {
            Map<String, String> pMap = parse((FullHttpRequest) msg);

            RpcInvocation invocation = new RpcInvocation();
            invocation.setMethodName(pMap.get("method"));
            invocation.setAttachment("path", pMap.get("interface"));
            invocation.setParameterTypes(pMap.get("parameterTypesString"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
            JsonUtils.writeObject(pMap.getOrDefault("parameter", ""), writer);
            invocation.setArguments(out.toByteArray());

            Request request = new Request(id);
            request.setVersion("2.0.0");
            request.setTwoWay(true);
            request.setData(invocation);
            targetChannel.writeAndFlush(request);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        targetChannel = client.getChannel();
    }

    private Map<String, String> parse(FullHttpRequest fullReq) throws IOException {
        HttpMethod method = fullReq.method();

        Map<String, String> parmMap = new HashMap<>();

        if (HttpMethod.GET == method) {
            QueryStringDecoder decoder = new QueryStringDecoder(fullReq.uri());
            decoder.parameters().entrySet().forEach( entry -> {
                // entry.getValue()是一个List, 只取第一个元素
                parmMap.put(entry.getKey(), entry.getValue().get(0));
            });
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

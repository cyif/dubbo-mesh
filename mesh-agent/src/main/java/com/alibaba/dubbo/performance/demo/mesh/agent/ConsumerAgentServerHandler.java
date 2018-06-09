package com.alibaba.dubbo.performance.demo.mesh.agent;

import com.alibaba.dubbo.performance.demo.mesh.agent.model.Agent;
import com.alibaba.dubbo.performance.demo.mesh.rpc.ConsumerRpcClient;
import com.alibaba.dubbo.performance.demo.mesh.rpc.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.mesh.rpc.model.Request;
import com.alibaba.dubbo.performance.demo.mesh.rpc.model.RpcInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
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
            Map<String, String> pMap = parse((FullHttpRequest) msg);

//            RpcInvocation invocation = new RpcInvocation();
//            invocation.setMethodName(pMap.get("method"));
//            invocation.setAttachment("path", pMap.get("interface"));
//            invocation.setParameterTypes(pMap.get("parameterTypesString"));
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
//            JsonUtils.writeObject(pMap.getOrDefault("parameter", ""), writer);
//            invocation.setArguments(out.toByteArray());

            long id = (channelId << 30) + IdGenerator.getInstance().getRequestId();
//            Request request = new Request(id);
//            request.setVersion("2.0.0");
//            request.setTwoWay(true);
//            request.setData(invocation);

            Agent.AgentRequest request = Agent.AgentRequest.newBuilder()
                    .setId(id)
                    .setMethodName(pMap.get("method"))
                    .setInterfaceName(pMap.get("interface"))
                    .setParameterTypesString(pMap.get("parameterTypesString"))
                    .setParameter(pMap.get("parameter")).build();
            logger.info("Request id : " + id + " content : " + request.toString());
            targetChannel.writeAndFlush(request);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelId = IdGenerator.getInstance().getChannelId();
        ConsumerAgentServer.channelMap.put(channelId, ctx.channel());
        targetChannel = client.getChannel();
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

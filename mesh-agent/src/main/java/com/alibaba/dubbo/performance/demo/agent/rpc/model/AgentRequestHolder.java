package com.alibaba.dubbo.performance.demo.agent.rpc.model;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ConcurrentHashMap;

public class AgentRequestHolder {

    private static ConcurrentHashMap<Long, Promise<Agent.AgentResponse>> processingRpc = new ConcurrentHashMap<>();

    public static void put(long requestId, Promise<Agent.AgentResponse> promise){
        processingRpc.put(requestId, promise);
    }

    public static Promise<Agent.AgentResponse> get(long requestId){
        return processingRpc.get(requestId);
    }

    public static Promise<Agent.AgentResponse> remove(long requestId){
        return processingRpc.remove(requestId);
    }
}

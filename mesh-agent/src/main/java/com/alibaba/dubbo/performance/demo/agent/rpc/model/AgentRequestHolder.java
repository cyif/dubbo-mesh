package com.alibaba.dubbo.performance.demo.agent.rpc.model;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.Promise;

public class AgentRequestHolder {

    private static FastThreadLocal<LongObjectHashMap<Promise<Agent.AgentResponse>>> processingRpc =
            new FastThreadLocal<LongObjectHashMap<Promise<Agent.AgentResponse>>>() {
                @Override
                protected LongObjectHashMap<Promise<Agent.AgentResponse>> initialValue() throws Exception {
                    return new LongObjectHashMap<>();
                }
            };

    public static void put(long requestId, Promise<Agent.AgentResponse> promise){
        processingRpc.get().put(requestId, promise);
    }

    public static Promise<Agent.AgentResponse> get(long requestId){
        return processingRpc.get().get(requestId);
    }

    public static Promise<Agent.AgentResponse> remove(long requestId){
        return processingRpc.get().remove(requestId);
    }
}

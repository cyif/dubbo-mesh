package com.alibaba.dubbo.performance.demo.agent.rpc.model;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.Promise;

public class AgentRequestHolder {

    private static FastThreadLocal<LongObjectHashMap<Promise>> processingRpc =
            new FastThreadLocal<LongObjectHashMap<Promise>>() {
                @Override
                protected LongObjectHashMap<Promise> initialValue() throws Exception {
                    return new LongObjectHashMap<>();
                }
            };

    public static void put(long requestId, Promise<Agent.AgentResponse> promise){
        processingRpc.get().put(requestId, promise);
    }

    public static Promise get(long requestId){
        return processingRpc.get().get(requestId);
    }

    public static Promise remove(long requestId){
        return processingRpc.get().remove(requestId);
    }
}

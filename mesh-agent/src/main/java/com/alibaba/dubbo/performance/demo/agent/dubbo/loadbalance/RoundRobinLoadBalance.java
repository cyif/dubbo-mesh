package com.alibaba.dubbo.performance.demo.agent.dubbo.loadbalance;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-29
 * Time: 下午2:22
 */
public class RoundRobinLoadBalance implements LoadBalance {

    private AtomicInteger count = new AtomicInteger();

    @Override
    public Endpoint select(List<Endpoint> endpoints) {
        return endpoints.get(count.incrementAndGet() % endpoints.size());
    }

}

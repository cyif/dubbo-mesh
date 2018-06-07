package com.alibaba.dubbo.performance.demo.agent.dubbo.loadbalance;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalance.class);

    private AtomicInteger count = new AtomicInteger();

    private List<Endpoint> endpoints;

    private int[] index = new int[100];

    private int totalWeight;

    public RoundRobinLoadBalance(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
        totalWeight = endpoints.stream().mapToInt(Endpoint::getWeight).sum();

        int k = 0;
        for (int i = 0; i < endpoints.size(); i++) {
            for (int j = 0; j < endpoints.get(i).getWeight(); j++) {
                index[k++] = i;
            }
        }
        for (int i = 0; i < k; i++) {
            logger.info("Index: " + index[i]);
        }

    }

    @Override
    public Endpoint select() {

        int order = count.incrementAndGet() % totalWeight;

        return endpoints.get(index[order]);
    }

}

package com.alibaba.dubbo.performance.demo.agent.dubbo.loadbalance;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-29
 * Time: 下午3:36
 */
public class RandomLoadBalance implements LoadBalance{

    private Random random = new Random();

    @Override
    public Endpoint select(List<Endpoint> endpoints) {
        return endpoints.get(random.nextInt(endpoints.size()));
    }
}

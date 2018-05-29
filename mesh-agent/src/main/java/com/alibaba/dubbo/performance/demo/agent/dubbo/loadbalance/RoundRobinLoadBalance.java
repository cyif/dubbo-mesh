package com.alibaba.dubbo.performance.demo.agent.dubbo.loadbalance;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-29
 * Time: 下午2:22
 */
public class RoundRobinLoadBalance implements LoadBalance {


    @Override
    public Endpoint select(List<Endpoint> endpoints) {
        return null;
    }

}

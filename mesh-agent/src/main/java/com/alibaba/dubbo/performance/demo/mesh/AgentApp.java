package com.alibaba.dubbo.performance.demo.mesh;

import com.alibaba.dubbo.performance.demo.mesh.agent.AgentConstant;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApp {

    public static void main(String[] args) {
        AgentConstant.AGENT_SERVER.run();
    }
}

package com.alibaba.dubbo.performance.demo.agent.dubbo.model;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-30
 * Time: 下午2:49
 */
public class AgentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;

    private String r_interface;

    private String r_method;

    private String r_parameterTypesString;

    private String r_parameter;

    public AgentRequest(long id) {
        this.id = id;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public String getR_interface() {
        return r_interface;
    }

    public void setR_interface(String r_interface) {
        this.r_interface = r_interface;
    }

    public String getR_method() {
        return r_method;
    }

    public void setR_method(String r_method) {
        this.r_method = r_method;
    }

    public String getR_parameterTypesString() {
        return r_parameterTypesString;
    }

    public void setR_parameterTypesString(String r_parameterTypesString) {
        this.r_parameterTypesString = r_parameterTypesString;
    }

    public String getR_parameter() {
        return r_parameter;
    }

    public void setR_parameter(String r_parameter) {
        this.r_parameter = r_parameter;
    }
}

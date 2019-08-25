package com.ourjoy.netty.tutorial.netty.protocol;

import java.io.Serializable;

/**
 * 消息头对象，这里只是举例了两个属性，真是场景可能需要以下字段：
 * user: 请求者的用户名
 * token：请求者的token, 验证合法性
 * interface：表示请求的是什么业务接口
 * version: 接口版本
 */
public class Header implements Serializable {

    private static final long serialVersionUID = 4190503285833908646L;
    private String token;
    //有些反序列化组件，需要知道反序列化的目标Class，可以通过这里告知
    private Class bodyClass;

    public Header() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Class getBodyClass() {
        return bodyClass;
    }

    public void setBodyClass(Class bodyClass) {
        this.bodyClass = bodyClass;
    }

    @Override
    public String toString() {
        return "Header{" +
                "token='" + token + '\'' +
                ", bodyClass=" + bodyClass +
                '}';
    }
}

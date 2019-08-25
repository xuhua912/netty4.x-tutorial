package com.ourjoy.netty.tutorial.netty.handler;

import com.ourjoy.netty.tutorial.netty.protocol.Protocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 自定义消息读取处理类
 */
public class ClientHandler extends SimpleChannelInboundHandler<Protocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protocol protocol) throws Exception {
        System.out.println(protocol);
    }
}
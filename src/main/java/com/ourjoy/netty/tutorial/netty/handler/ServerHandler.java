package com.ourjoy.netty.tutorial.netty.handler;

import com.ourjoy.netty.tutorial.netty.protocol.Protocol;
import com.ourjoy.netty.tutorial.netty.dto.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义消息读取处理类
 */
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<Protocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protocol protocol) throws Exception {
        log.info("receive msg: " + protocol);

        //这里简单的将读取到的消息修改一下后，回写给客户端
        protocol.getHeader().setToken("i'am response token");
        User user = (User) protocol.getBody();
        user.setAge(32);

        //写入数据，并立即将消息刷出去
        ctx.writeAndFlush(protocol);
    }

}

package com.ourjoy.netty.tutorial.netty;

import com.ourjoy.netty.tutorial.netty.codec.MyDecoder;
import com.ourjoy.netty.tutorial.netty.codec.MyEncoder;
import com.ourjoy.netty.tutorial.netty.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {

    private static final Integer PORT = 8888;
    private static final Integer BOSS_THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2;
    private static final Integer WORK_THREAD_NUM = 100;

    public static void start() throws InterruptedException {

        //创建boss线程组，用于接收客户端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(BOSS_THREAD_NUM);
        //创建work线程组，用于处理IO操作和业务逻辑处理
        EventLoopGroup workGroup = new NioEventLoopGroup(WORK_THREAD_NUM);

        //创建服务端启动引导类，该类是为了简化编程，启动所需要的参数都通过它传入进行整合
        ServerBootstrap bootstrap = new ServerBootstrap();
        //设置两个工作组
        bootstrap.group(bossGroup, workGroup)
                //设置所需要实例化的Channel实现，这里是服务端，所以是NioServerSocketChannel，如果是Linux，可以是EpollServerSocketChannel
                .channel(NioServerSocketChannel.class)
                //设置childHandler，它是给新创建出来的SocketChannel对象使用的，ChannelInitializer的作用是初始化Channel，通常会再initChannel中添加一系列的ChannelHandler
                .childHandler(new ChannelInitializer<Channel>() {

                    /**
                     * initChannel在Channel被注册完成之后调用一次，在本方法调用完成返回后，本ChannelInitializer实例将从Pipeline中删除掉
                     * @param ch 当前Channel
                     * @throws Exception
                     */
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        //拿到当前Channel对应的Pipeline实例，它是在Channel创建的时候一并创建的
                        ChannelPipeline pipeline = ch.pipeline();
                        //自定义编码器，这里将对象转换为二进制
                        pipeline.addLast(new MyEncoder())
                                //自定义解码器，对消息进行解码，这里可以将消息解码为对象
                                .addLast(new MyDecoder())
                                //自定义业务处理类，对已经解码的对象进行处理
                                .addLast(new ServerHandler());
                    }
                });

        //绑定端口，并同步阻塞到绑定完成
        ChannelFuture channelFuture = bootstrap.bind(PORT).sync();
        log.info("Netty server started, bind on port: " + PORT);

        //同步阻塞只到Channel关闭后，方法返回，主流程结束
        channelFuture.channel().closeFuture().sync();

    }

    public static void main(String[] args) throws InterruptedException {
        //启动NettyServer
        NettyServer.start();
    }

}

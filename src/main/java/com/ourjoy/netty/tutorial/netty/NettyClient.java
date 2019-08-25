package com.ourjoy.netty.tutorial.netty;

import com.ourjoy.netty.tutorial.netty.codec.MyDecoder;
import com.ourjoy.netty.tutorial.netty.codec.MyEncoder;
import com.ourjoy.netty.tutorial.netty.protocol.Header;
import com.ourjoy.netty.tutorial.netty.protocol.Protocol;
import com.ourjoy.netty.tutorial.netty.dto.User;
import com.ourjoy.netty.tutorial.netty.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

    private static ChannelFuture channelFuture = null;
    private static final Integer WORK_THREAD_NUM = 100;

    public static void start() throws InterruptedException {
        //新建工作线程组
        EventLoopGroup workGroup = new NioEventLoopGroup(WORK_THREAD_NUM);
        //新建启动引导类，该类是为了简化编程，启动所需要的参数都通过它传入进行整合
        Bootstrap bootstrap = new Bootstrap();
        //设置工作线程组，这里和Server端不一样，只需要设置一个
        bootstrap.group(workGroup)
                //设置所需要实例化的Channel实现，这里是客户端，所以是NioSocketChannel，如果是Linux，可以是EpollSocketChannel
                .channel(NioSocketChannel.class)
                //设置childHandler，它是给新创建出来的SocketChannel对象使用的，ChannelInitializer的作用是初始化Channel，通常会再initChannel中添加一系列的ChannelHandler
                .handler(new ChannelInitializer() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        //拿到当前Channel对应的Pipeline实例，它是在Channel创建的时候一并创建的
                        ChannelPipeline pipeline = ch.pipeline();
                        //自定义编码器，这里将对象转换为二进制
                        pipeline.addLast(new MyEncoder())
                                //自定义解码器，对消息进行解码，这里可以将消息解码为对象
                                .addLast(new MyDecoder())
                                //自定义业务处理类，对已经解码的对象进行处理
                                .addLast(new ClientHandler());
                    }
                });

        //连接到服务器，并同步阻塞到绑定完成，channelFuture作为一个句柄，可以用来进程IO操作
        channelFuture = bootstrap.connect("127.0.0.1", 8888).sync();

    }

    public static void main(String[] args) throws InterruptedException {
        //启动客户端
        NettyClient.start();

        Header header = new Header();
        header.setToken("i am token");
        header.setBodyClass(User.class);

        User user = new User("zhangsan", 100);

        Protocol protocol = new Protocol();
        protocol.setMagicNum(0xABABABAB);
        protocol.setHeader(header);
        protocol.setBody(user);

        //将protocol对象发送至服务端
        channelFuture.channel().writeAndFlush(protocol);
    }

}

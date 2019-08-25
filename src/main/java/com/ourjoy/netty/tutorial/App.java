package com.ourjoy.netty.tutorial;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.epoll.EpollEventLoopGroup;

import java.net.Socket;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        ServerBootstrap bootstrap = new ServerBootstrap();

        EpollEventLoopGroup boss = new EpollEventLoopGroup();

        Socket socket = new Socket();
//        socke

    }
}

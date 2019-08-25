package com.ourjoy.netty.tutorial.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NioClient {

    public static void main(String[] args) throws IOException {
        start();
    }

    private static void start() throws IOException {

        //打开一个客户端端的通道
        SocketChannel socketChannel = SocketChannel.open();
        //与服务端建立连接
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8888));
        //将阻塞模型设置为非阻塞，这里不设置，会抛出IllegalBlockingModeException
        socketChannel.configureBlocking(false);
        log.info("connect 127.0.0.1:8888 success......");

        //打开一个选择器，又称多路复用器，它是专门用来轮训IO事件是否就绪的组件
        Selector selector = Selector.open();

        //将通道的READ，WRITE事件注册到选择器
        //这里模拟场景是客户端先要往服务端发消息，所以先注册了WRITE事件
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        while (true) {
            //选择操作，轮训所有注册到本选择器的通道，任何一个通道的感兴趣事件就绪，就会返回，另外两种返回场景是wakeup方法被调用，或者线程的interrupt方法被调用
            if (0 == selector.select()) {
                log.info("no event happened...");
                continue;
            }
            select(selector);
        }
    }

    private static void select(Selector selector) throws IOException {

        //拿到所有已经就绪的SelectionKey，进行迭代处理
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            try {
                //根据SelectionKey的事件类型，分别处理
                if (key.isReadable()) {
                    handleRead(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                }

                //这里处理完一个事件后，需要手动删除掉，不会自动删除
                iterator.remove();
            } catch (IOException e) {
                log.info("nio select IOException.", e);
                key.interestOps(0);
                key.channel().close();
                break;
            }
        }

    }

    /**
     * 处理写事件
     * @param key SelectionKey
     * @throws IOException
     */
    private static void handleWrite(SelectionKey key) throws IOException {
        log.info("handleWrite......");

        SocketChannel socketChannel = (SocketChannel) key.channel();

        String msg = "你好，NIO, " + new Date();
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);

        //翻转buffer，这一步很重要，是将buffer从当前的写模式切换为读模式，即position设置为0，limit设置为消息总长度
        buffer.flip();

        socketChannel.write(buffer);
        log.info("send msg to server: " + msg);

        //注销通道的写事件，若不注销，则在缓冲区未满情况下回无限触发写事件
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

    /**
     * 处理读事件
     * @param key
     * @throws IOException
     */
    private static void handleRead(SelectionKey key) throws IOException {
        log.info("handleRead......");
        SocketChannel socketChannel = (SocketChannel) key.channel();

        ByteBuffer lenBuffer = ByteBuffer.allocate(4);
        int readLen = socketChannel.read(lenBuffer);
        if (readLen == 4) {
            Integer len = ByteBuffer.wrap(lenBuffer.array()).getInt();

            ByteBuffer dataBuffer = ByteBuffer.allocate(len);
            socketChannel.read(dataBuffer);
            String msg = new String(dataBuffer.array(), StandardCharsets.UTF_8);
            log.info("read server response: " + msg);
        }

        try {
            log.info("sleep 3s......");
            log.info("---------------------------------------------------------------------------------------------------");
            TimeUnit.SECONDS.sleep(3L);

            //处理完读事件后，继续注册写事件，往服务端写消息
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

}

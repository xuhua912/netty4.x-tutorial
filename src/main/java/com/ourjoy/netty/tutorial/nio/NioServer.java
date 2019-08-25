package com.ourjoy.netty.tutorial.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NioServer {

    public static void main(String[] args) throws IOException {
        start();
    }

    private static void start() throws IOException {
        //打开一个服务端的通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //将通道绑定到8888端口
        serverSocketChannel.bind(new InetSocketAddress(8888));
        //将阻塞模型设置为非阻塞，这里不设置，会抛出IllegalBlockingModeException
        serverSocketChannel.configureBlocking(false);

        //打开一个选择器，又称多路复用器，它是专门用来轮训IO事件是否就绪的组件
        Selector selector = Selector.open();
        //将服务端通道的ACCEPT事件注册到选择器，当有客户端与服务端建立TCP三次握手成功后，将触发该事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        log.info("server started with port 8888");

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
                //事件总共分类4种，ACCEPT、CONNECT、READ、WRITE, 其中ACCEPT是服务端事件，CONNECT是客户端事件，READ、WRITE两端均可
                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                }

                //这里处理完一个事件后，需要手动删除掉，不会自动删除
                iterator.remove();
            } catch (IOException e) {
                log.info("nio select IOException.", e);
                key.interestOps(0);
                // 若发生IOException，则断开通道
                key.channel().close();
                break;
            }
        }

    }

    /**
     * 处理连接接入事件
     * @param key SelectionKey
     * @throws IOException
     */
    private static void handleAccept(SelectionKey key) throws IOException {
        log.info("handleAccept......, accept it...");

        //这里的key.channel()返回的一定是ServerSocketChannel，不会是SocketChannel
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        //调用accept()方法，接入新连接，生成SocketChannel对象，用于后续的读写操作
        SocketChannel socketChannel = serverSocketChannel.accept();
        //将阻塞模型设置为非阻塞，这里不设置，会抛出IllegalBlockingModeException
        socketChannel.configureBlocking(false);

        log.info("add read|write select key to socket channel");
        //注册读事件到选择器
        socketChannel.register(key.selector(), SelectionKey.OP_READ);
    }

    /**
     * 处理读事件
     * @param key SelectionKey
     * @throws IOException
     */
    private static void handleRead(SelectionKey key) throws IOException {
        log.info("handleRead......");
        //这里的key.channel()返回的一定是SocketChannel
        SocketChannel socketChannel = (SocketChannel) key.channel();

        //先定义一个4个字节的buffer空间
        ByteBuffer lenBuffer = ByteBuffer.allocate(4);
        //接着从通道中读取4个字节到lenBuffer
        int readLen = socketChannel.read(lenBuffer);
        if (readLen == 4) {
            //将这4个字节转化为Integer类型，根据我们自定义的报文协议，它表示整体消息的长度
            Integer len = ByteBuffer.wrap(lenBuffer.array()).getInt();

            //然后定义一个len长度的buffer空间
            ByteBuffer dataBuffer = ByteBuffer.allocate(len);
            //接着从通道中读取len个字节到dataBuffer,这len个字节的内容就是我们实际的消息体
            socketChannel.read(dataBuffer);
            //将消息体转化为字符串
            String msg = new String(dataBuffer.array(), StandardCharsets.UTF_8);
            log.info("read client msg: " + msg);

            //这里拿到客户端的消息后，我们直接原样返回，将它添加到key的附件上
            key.attach(msg);
            //注册通道的写事件,一定要在需要写的时候才注册写事件，写完后立即注销写事件，否则只要内核缓冲区未满，就会一直触发写事件，CPU会飙高
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        }

    }

    /**
     * 处理写事件
     * @param key SelectionKey
     * @throws IOException
     */
    private static void handleWrite(SelectionKey key) throws IOException {
        log.info("handleWrite......");

        if (null != key.attachment()) {

            //这里我们拿到handleRead方法中添加的附件，即消息体
            String msg = (String) key.attachment();

            byte[] data = msg.getBytes(StandardCharsets.UTF_8);
            //申请buffer空间，长度是4+消息体长度
            ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
            //先往通道写入4个字节的消息体长度
            buffer.putInt(data.length);
            //再往通道写入实际的消息体
            buffer.put(data);
            //翻转buffer，这一步很重要，是将buffer从当前的写模式切换为读模式，即position设置为0，limit设置为消息总长度
            buffer.flip();

            SocketChannel socketChannel = (SocketChannel) key.channel();
            //往通道写入buffer里的字节
            socketChannel.write(buffer);

            log.info("send msg to client: " + msg);
            log.info("---------------------------------------------------------------------------------------------------");

        }
        //注销通道的写事件，若不注销，则在缓冲区未满情况下回无限触发写事件
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);

    }

}

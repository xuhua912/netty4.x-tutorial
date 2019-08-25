package com.ourjoy.netty.tutorial.netty.codec;

import com.ourjoy.netty.tutorial.netty.protocol.Protocol;
import com.ourjoy.netty.tutorial.netty.serializer.ObjectSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义编码器，将Protocol对象编码为二进制字节数组
 */
public class MyEncoder extends MessageToByteEncoder<Protocol> {

    /**
     * 我们这里将protocol对象序列化为5部分，分别如下：
     * 消息总长度：长度设计占2个字节，值表示整个消息包所占的字节数
     * 魔数：长度设计占4个字节，固定为0xABABABAB
     * header长度：长度设计占2个字节，值表示header对象序列化后byte[]的length
     * header字节数组：表示header对象序列化后的byte[]
     * body字节数组：表示body对象序列化后的byte[]
     *
     * @param ctx
     * @param protocol
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Protocol protocol, ByteBuf out) throws Exception {
        //将Header对象序列化为byte[]
        byte[] headerBytes = ObjectSerializer.toArray(protocol.getHeader());
        //将Body对象序列化为byte[]
        byte[] bodyBytes = ObjectSerializer.toArray(protocol.getBody());

        //写入消息总长度，值=自身占2byte + 魔数占4byte + header长度占2byte + headerBytes.length + bodyBytes.length
        out.writeShort(2 + 4 + 2 + headerBytes.length + bodyBytes.length);
        //写入魔数0xABABABAB
        out.writeInt(protocol.getMagicNum());
        //写入header长度
        out.writeShort(headerBytes.length);
        //写入headerBytes
        out.writeBytes(headerBytes);
        //写入bodyBytes
        out.writeBytes(bodyBytes);

        //**注意这里只是编码，将数据写入到ByteBuf即可，Netty后续流程会将数据写入Channel**
    }

}
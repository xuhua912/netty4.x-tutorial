package com.ourjoy.netty.tutorial.netty.codec;

import com.ourjoy.netty.tutorial.netty.protocol.Header;
import com.ourjoy.netty.tutorial.netty.protocol.Protocol;
import com.ourjoy.netty.tutorial.netty.serializer.ObjectSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * LengthFieldBasedFrameDecoder解码器是通过消息头部的一个长度为lengthFieldLength字节的消息，来判断消息体或整体消息的长度，从而读取完整整体消息
 */
@Slf4j
public class MyDecoder extends LengthFieldBasedFrameDecoder {

    public MyDecoder() {

        /**
         * ❉❉ 下面的长度单位都是字节 ❉❉
         * lengthField  长度字段，它可能是消息体(Body)长度，也可能是整个消息(Frame)的长度，不同场景，lengthAdjustment的值会不同，解码器根据lengthField来解码消息。
         *
         * 案例：
         *    BEFORE DECODE (28 bytes)                                           AFTER DECODE (26 bytes)
         *    +--------+------------+-----------+------------+----------------+      +------------+-----------+------------+----------------+
         *    | LEN    | MAGIC      | HDR LEN   | Header     | Body           |----->| MAGIC      | HDR LEN   | Header     | Body           |
         *    | 0x001C | 0xABABABAB | 0x0008    | "WA HA HA" | "HELLO, WORLD" |      | 0xABABABAB | 0x0008    | "WA HA HA" | "HELLO, WORLD" |
         *    +--------+-----------+-----------+------------+----------------+      +------------+-----------+------------+----------------+
         *    注意LEN的值为0x001C，即28，那么LEN表示的是整个消息的长度，如果为0x001A（26），那么表示的从LEN结束位置以后剩余的长度
         *
         * maxFrameLength    一次发送的数据包的最大长度，超过长度的消息，就被丢弃不作处理
         * lengthFieldOffset lengthField的起始位置，这个位置类似于数组的下标，从0开始计算， 本案例为0
         * lengthFieldLength lengthField的长度，本案例为2
         * lengthAdjustment  长度起始位置偏移量，它的计算公式：lengthAdjustment = 消息总长度 - lengthFieldOffset - lengthFieldLength - lengthField
         *                   本案例为lengthAdjustment = 28 - 0 - 2 - 28 = -2， 这个-2其实就是lengthField开始计算长度的位置下标减去LEN结束位置下标
         * initialBytesToStrip 需要跳过的字节数，即从整个消息中跳过initialBytesToStrip个字节的内容，只返回剩下的字节信息，上例中需要跳过LEN，即为2
         *
         */
        super(1024 * 1024, 0, 2, -2, 2);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            //先调用父类的decode方法，拿到处理后的结果，本案例是拿到LEN后面的内容，LEN会被丢掉
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                log.error("frame is null");
                return null;
            }

            ByteBuffer byteBuffer = frame.nioBuffer();
            //byteBuffer的容量，实际就是我们要的数据的长度
            Integer capacity = byteBuffer.capacity();

            //已经通过父类解码器的initialBytesToStrip属性，跳过了LEN字段，直接读取魔数
            Integer magicNum = byteBuffer.getInt();
            if (0xABABABAB == magicNum) {
                //读取headerLen,便于完整读取Header的二进制数组
                Short headerLen = byteBuffer.getShort();

                //创建一个byte[]，来存储header
                byte[] headerBytes = new byte[headerLen];
                //从byteBuffer中读取headerLen长度的字节到headerBytes数组中
                byteBuffer.get(headerBytes);
                //将headerBytes数组反序列化为Header对象
                Header header = (Header) ObjectSerializer.toObject(headerBytes);

                //计算出bodyLen
                Integer bodyLen = capacity - 4 - 2 - headerBytes.length;
                byte[] bodyBytes = new byte[bodyLen];
                byteBuffer.get(bodyBytes);
                //将bodyBytes数组反序列化为Header对象
                Object body = ObjectSerializer.toObject(bodyBytes);

                //构建protocol对象返回
                Protocol protocol = new Protocol();
                protocol.setMagicNum(magicNum);
                protocol.setLength(capacity.shortValue());
                protocol.setHeaderLength(headerLen);
                protocol.setHeader(header);
                protocol.setBody(body);
                return protocol;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ctx.channel().close();
        } finally {
            if (null != frame) {
                frame.release();
            }
        }

        return null;
    }
}
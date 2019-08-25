package com.ourjoy.netty.tutorial.netty.protocol;

import java.io.Serializable;

public class Protocol implements Serializable {

    private static final long serialVersionUID = 1658637343288015366L;
    //魔数
    private Integer magicNum;
    //消息总长度
    private Short length;
    //header长度
    private Short headerLength;
    //Header对象
    private Header header;
    //Body对象
    private Object body;

    public Protocol() {
    }

    public Integer getMagicNum() {
        return magicNum;
    }

    public void setMagicNum(Integer magicNum) {
        this.magicNum = magicNum;
    }

    public Short getLength() {
        return length;
    }

    public void setLength(Short length) {
        this.length = length;
    }

    public Short getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(Short headerLength) {
        this.headerLength = headerLength;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Protocol{" +
                "magicNum=" + magicNum +
                ", length=" + length +
                ", headerLength=" + headerLength +
                ", header=" + header +
                ", body=" + body +
                '}';
    }
}

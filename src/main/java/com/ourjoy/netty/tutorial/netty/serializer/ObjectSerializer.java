package com.ourjoy.netty.tutorial.netty.serializer;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 对象序列化组件，这里直接使用了JDK自带的序列化技术，生产场景不建议使用
 */
@Slf4j
public class ObjectSerializer {

    /**
     * 将ByteArrayOutputStream包装成ObjectOutputStream后，写入对象，然后通过toByteArray()方法取回byte[]
     * @param obj
     * @return
     */
    public static byte[] toArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return bytes;
    }

    /**
     * 数组转对象
     * 将ByteArrayInputStream包装成ObjectInputStream后，读入byte[]，然后通过readObject方法取回对象
     * @param bytes
     * @return
     */
    public static Object toObject(byte[] bytes) {
        Object obj = null;
        ObjectInputStream ois = null;
        ByteArrayInputStream bis = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return obj;
    }


}
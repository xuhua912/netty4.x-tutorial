package com.ourjoy.netty.tutorial.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class BioServer {

    public static void main(String[] args) throws IOException {
        start();
    }

    public static void start() throws IOException {

        // 创建ServerSocket，绑定到8888端口上
        ServerSocket serverSocket = new ServerSocket(8888);
        log.info("server start successful..., port 8888");

        //这里死循环
        while(true) {
            try {
                // 接受新连接接入，这里是阻塞操作
                Socket socket = serverSocket.accept();
                log.info("received socket connect...");

                // 将创建的socket交给一个线程去处理，这里通常使用线程池
                new Thread(new ServerSocketHandler(socket)).start();
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    }

    static class ServerSocketHandler implements Runnable {
        private Socket socket;

        public ServerSocketHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (true) {
                handler();
            }
        }

        private void handler() {
            InputStream in = null;
            OutputStream out = null;
            BufferedReader br = null;
            BufferedWriter bw = null;
            try {
                // 从socket中拿到输入输出流
                in = socket.getInputStream();
                out = socket.getOutputStream();

                // 将输入流包装为BufferedReader
                br = new BufferedReader(new InputStreamReader(in));

                // 一次读取一行，这里是阻塞操作
                String line = br.readLine();
                log.info("received client msg: " + line);

                // 响应
                response(out, line);
            } catch (IOException e) {
                log.info(e.getMessage());
            } finally {
                // close in out  br bw ....
            }
        }

        private void response(OutputStream out, String msg) {
            try {
                // 将输出流包装为BufferedWriter
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

                // 输出msg，并已\r\n结尾，表示一行结束，这里是阻塞操作
                bw.write(msg + "\r\n");
                // 将缓冲区数据强制刷出去
                bw.flush();
                log.info("write response to client completed.");
                log.info("-----------------------------------------------------------------------------------");
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        }
    }
}

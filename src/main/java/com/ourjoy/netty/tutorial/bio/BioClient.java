package com.ourjoy.netty.tutorial.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
public class BioClient {

    public static void main(String[] args) throws IOException {
        start();
    }

    private static void start() throws IOException {

        // 与服务端建立连接，端口8888
        Socket socket = new Socket("127.0.0.1", 8888);
        try {
            // 将新建立的连接交给一个线程去处理
            new Thread(new SocketHandler(socket)).start();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    static class SocketHandler implements Runnable {
        private Socket socket;

        public SocketHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // 循环处理
            while (true) {
                log.info("-----------------------------------------------------------------------------------");
                log.info("pls input words...");
                handler();
            }
        }

        private void handler() {

            BufferedReader br = null;
            BufferedWriter bw = null;
            try {
                // 包装BufferedReader  BufferedWriter,这里的System.in是系统输入流，可直接在控制台输入
                br = new BufferedReader(new InputStreamReader(System.in));
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // 一次读取一行，这里是阻塞操作
                String input = br.readLine();

                // 将用户输入的信息，输出到服务端，以\r\n结尾，这里是阻塞操作
                bw.write(input + "\r\n");
                // 将缓冲区数据强制刷出去
                bw.flush();
                log.info("user input words: " + input + ", write user input to server");

                // 接受服务端的响应
                receiveResponse(socket.getInputStream());
            } catch (IOException e) {
                log.info(e.getMessage());
            } finally {
                // close in out  br bw ....
            }
        }

        private void receiveResponse(InputStream in) throws IOException {
            // 这里的in，是socket的输入流，不再是System.in
            BufferedReader br2 = new BufferedReader(new InputStreamReader(in));
            // 一次读取一行，这里是阻塞操作
            String line = br2.readLine();
            log.info("received server message: " + line);
        }

    }

}

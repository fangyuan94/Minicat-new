package com.fy.minicat.connector;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 用于解析endpoint传来的流信息 转化为http协议
 * @author fangyuan
 */
public class Http11Processor  {

    //默认读缓冲区大小
    public static final int readBuffSize = 8192;

    //默认写缓存区大小
    public static final int writeBuffSize = 8192;

    private FyAdapter adapter;

    public void setAdapter(FyAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * 为响应码200提供请求头信息
     * @return
     */
    public static String getHttpHeader200(long contentLength) {
        return "HTTP/1.1 200 OK \n" +
                "Content-Type: text/html \n" +
                "Content-Length: " + contentLength + " \n" +
                "\r\n";
    }

    /**
     * 为响应码404提供请求头信息(此处也包含了数据内容)
     * @return
     */
    public static String getHttpHeader404() {
        String str404 = "<h1>404 not found</h1>";
        return "HTTP/1.1 404 NOT Found \n" +
                "Content-Type: text/html \n" +
                "Content-Length: " + str404.getBytes().length + " \n" +
                "\r\n" + str404;
    }

    /**
     * 处理
     * @param sk
     */
    public void service(SelectionKey sk) throws IOException {

        SocketChannel socketChannel = (SocketChannel) sk.channel();

        if(!socketChannel.isOpen()){
            System.out.println("流被关闭了");

            return;
        }

        ByteBuffer buffer = ByteBuffer.allocate(readBuffSize);
        StringBuilder body = new StringBuilder();
        try {
            //读取数据
            while (socketChannel.read(buffer) > 0) {
                body.append(new String(buffer.array()));
                buffer.clear();
            }
            if(body.length() == 0){
                sk.cancel();
                return;
            }

            // 获取第一行请求头信息
            String firstLineStr = body.toString().split("\\n")[0];  // GET / HTTP/1.1
            System.out.println("读取http头信息======================>"+firstLineStr);

            String[] strings = firstLineStr.split(" ");
            //读取数据 变更为Request
            Request request = new Request(strings[0],strings[1]);

            ServletOutputStream minicatServletOutputStream = new MinicatServletOutputStream(socketChannel);

            Response response = new Response(sk,minicatServletOutputStream);

            adapter.service(request,response);

        } catch (Exception e) {
            //客户端关闭了
            e.printStackTrace();
            sk.cancel();
            System.out.println("客戶端已断开");
        }


    }
}

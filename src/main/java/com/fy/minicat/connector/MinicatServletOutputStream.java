package com.fy.minicat.connector;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 写入最终结果
 */
public class MinicatServletOutputStream extends ServletOutputStream {

    private SocketChannel socketChannel;

    public MinicatServletOutputStream(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public void write(int b) throws IOException {


    }

    //写入响应
    @Override
    public void write(byte[] b) throws IOException {
        ByteBuffer bf = ByteBuffer.allocate(Http11Processor.writeBuffSize);
        bf.put(Http11Processor.getHttpHeader200(b.length).getBytes());
        bf.put(b);
        bf.flip();
        socketChannel.write(bf);
        socketChannel.close();
    }
}

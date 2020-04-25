package com.fy.minicat.utils;

import com.fy.minicat.connector.Http11Processor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class StaticResourceUtil {

    /**
     * 获取静态资源文件的绝对路径
     * @param path
     * @return
     */
    public static String getAbsolutePath(String path) {
        String absolutePath = StaticResourceUtil.class.getResource("/").getPath();
        return absolutePath.replaceAll("\\\\","/") + path;
    }

    public static void outputStaticResource(FileInputStream fileInputStream, SocketChannel socketChannel) throws IOException {

        int count = 0;
        while(count == 0) {
            count = fileInputStream.available();
        }

        FileChannel channel = fileInputStream.getChannel();

        int resourceSize = fileInputStream.available();


        ByteBuffer bf = ByteBuffer.allocate(Http11Processor.writeBuffSize);
        //bf.flip();
        bf.put(Http11Processor.getHttpHeader200(resourceSize).getBytes());
        bf.flip();
        socketChannel.write(bf);
        bf.clear();
        int length = -1;
        while ((length = channel.read(bf)) != -1) {
            //写入数据
            bf.flip();
            socketChannel.write(bf);
            /*
             * 注意，读取后，将位置置为0，将limit置为容量, 以备下次读入到字节缓冲中，从0开始存储
             */
            bf.clear();
        }
        socketChannel.close();
        channel.close();
    }


}

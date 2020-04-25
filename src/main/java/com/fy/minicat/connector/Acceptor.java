package com.fy.minicat.connector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 用于socket线程
 * @author fangyuan
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Acceptor implements Runnable {

    private NIOEndPoint nioEndPoint;

    @Override
    public void run() {

        //接受请求
        while(nioEndPoint.isRunning()){

            SocketChannel socket = null;
            try {
                //阻塞获取SocketChannel
               socket = nioEndPoint.getServerSock().accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(nioEndPoint.isRunning()){
                //处理连接
                nioEndPoint.setSocketOptions(socket);
            }

        }
    }
}

package com.fy.minicat.startup;

import com.fy.minicat.Minicat;

/**
 * 启动类
 * @author fangyuan
 */
public final class Bootstrap {

   private static Minicat minicat  = new Minicat() ;

    //初始化
    public static void init(){
        minicat.init();
    }

    //启动
    public static void start()  {
        minicat.start();
        //阻塞主进程
        try {
            //阻塞主线程 通过各子进程接受数据
            synchronized(Bootstrap.class){
                Bootstrap.class.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //停止
    public static void stop(){
        minicat.stop();
    }

    public static void main(String[] args) {
        init();
        start();
    }


    public void restart(){
        Bootstrap.this.stop();
        Bootstrap.this.start();
    }
}

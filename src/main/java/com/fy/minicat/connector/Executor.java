package com.fy.minicat.connector;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**-
 * 连接pool 单例
 */
public class Executor {

    public static final ThreadPoolExecutor threadPoolExecutor ;

    static {

        int corePoolSize = 50;
        int maximumPoolSize =100;
        long keepAliveTime =60;
        //线程登记等级
        int priority = 5;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue()  ;
        ThreadFactory threadFactory = new ThreadFactory() {
            //计算当前线程数
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {

                Thread thread = new Thread(r);
                //设置执行线程名称
                thread.setName("minicat-exec-"+threadNumber.getAndIncrement());
                thread.setPriority(priority);
                thread.setDaemon(true);
                return thread;
            }
        };
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,threadFactory);
    }

    public static ThreadPoolExecutor getExecutor(){
        return threadPoolExecutor;
    }

}

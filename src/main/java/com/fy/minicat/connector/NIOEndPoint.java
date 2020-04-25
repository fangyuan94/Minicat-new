package com.fy.minicat.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 端点类 用于接受socket请求
 * @author fangyaun
 */
public class NIOEndPoint implements Container {

    private  Poller poller;

    private Acceptor acceptor;

    //默认ip
    private final String address = "localhost";

    //默认端口
    private final int port = 8081;


    //ServerSocketChannel
    private volatile ServerSocketChannel serverSock = null;


    //httpNioProtocolHandler用户处理
    private HttpNioProtocolHandler httpNioProtocolHandler;

    public HttpNioProtocolHandler getHttpNioProtocolHandler() {
        return httpNioProtocolHandler;
    }

    public void setHttpNioProtocolHandler(HttpNioProtocolHandler httpNioProtocolHandler) {
        this.httpNioProtocolHandler = httpNioProtocolHandler;
    }

    //当前服务是否在运行
    protected volatile boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public ServerSocketChannel getServerSock() {
        return serverSock;
    }


    @Override
    public void init() throws Exception {
        //初始化serverSock
        serverSock = ServerSocketChannel.open();
        //注册一个轮询器
        poller = new Poller();
        bind();
    }

    //bind IP和端口号
    private void bind() throws IOException {
        InetSocketAddress addr = new InetSocketAddress(address, port);
        //挂起连接最大数
        int acceptCount = 100;
        //阻塞模式 默认为阻塞的
        serverSock.configureBlocking(true);
        serverSock.bind(addr,acceptCount);
    }

    public static class PollerEvent implements Runnable {

        private SocketChannel socket;

        private Selector selector;

        public PollerEvent(SocketChannel socket ,Selector selector) {
            this.socket = socket;
            this.selector = selector;
        }

        @Override
        public void run() {
            try {
                System.out.println("这里执行-----------");
                socket.register(this.selector, SelectionKey.OP_READ,this.socket);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 轮询器 用于noi中监控
     */
    public class Poller implements Runnable{

        //Selector
        private Selector selector;

        private volatile int keyCount = 0;

        //用于控制
        private AtomicLong wakeupCounter = new AtomicLong(0);

        private int selectorTimeout = 3000;

        private final ConcurrentLinkedQueue<PollerEvent> events =
                new ConcurrentLinkedQueue<>();

        public Poller() throws IOException {
            this.selector = Selector.open();
        }

        /**
         * 像selector 注册socket
         * @param socket
         */
        public void register(final SocketChannel socket) throws ClosedChannelException {

            PollerEvent pollerEvent = new PollerEvent(socket,this.selector);
            this.addEvent(pollerEvent);
        }

        private void addEvent(PollerEvent event) {
            events.offer(event);

            if (wakeupCounter.incrementAndGet() == 0) {
                selector.wakeup();
            }
        }

        public boolean events() {
            boolean result = false;
            PollerEvent pe = null;
            for (int i = 0, size = events.size(); i < size && (pe = events.poll()) != null; i++ ) {
                result = true;
                try {
                    pe.run();
                } catch ( Throwable x ) {
                    x.printStackTrace();
                }
            }
            return result;
        }

        @Override
        public void run() {

            //监控Selector是否有数据到达
            while(true){
                boolean hasEvents = false;
                try {

                    hasEvents = events();
                    //
                    if(wakeupCounter.getAndSet(-1)>0){
                        //非阻塞
                        keyCount = selector.selectNow();
                    }else{
                        //阻塞线程 直到有线程到达 定义超时事件
                        keyCount = selector.select(selectorTimeout);
                    }
                    //重新置为0
                    wakeupCounter.set(0);

                    if (keyCount == 0) {
                        hasEvents = (hasEvents | events());
                    }

                    System.out.println("---------------------------"+keyCount);
                    Iterator<SelectionKey> iterator =
                            keyCount > 0 ? selector.selectedKeys().iterator() : null;

                    while (iterator != null && iterator.hasNext()) {

                        SelectionKey sk = iterator.next();
                        try{

                            // 判断事件类型，做对应的处理
//                            if (sk.isAcceptable()) {
//                                ServerSocketChannel ssChannel = (ServerSocketChannel) sk.channel();
//                                SocketChannel socketChannel = ssChannel.accept();
//                                System.out.println("准备接受处理请求："+ socketChannel.getRemoteAddress());
//                                // 获取客户端的数据
//                                // 设置非阻塞状态
//                                socketChannel.configureBlocking(false);
//                                // 注册到selector(通道选择器)
//                                socketChannel.register(selector, SelectionKey.OP_READ);
//                            }else
                                if(sk.isReadable()){
                                //删除当前键，避免重复消费
                                iterator.remove();
                                processKey(sk);
                            }else{
                                    iterator.remove();
                                    sk.cancel();
                                }

                        }catch (CancelledKeyException c){
                            sk.cancel();
                            SocketChannel socketChannel = (SocketChannel) sk.attachment();
                            socketChannel.close();
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        /**
         * 处理数据
         * @param sk
         */
        private void processKey(SelectionKey sk) {

            if(sk.isValid() ){
                //处理读
                if(sk.isReadable() ){
                    //处理tcp携带报文信息
                    processSocket(sk,SelectionKey.OP_READ);
                    //处理写
                }else if( sk.isWritable()){
                    processSocket(sk,SelectionKey.OP_WRITE);
                }

            }else{
                sk.cancel();
                try {
                    if(sk.channel()!=null){
                        sk.channel().close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 调用线程Http11Processor处理socket
     * @param sk
     * @param event
     */
    private boolean processSocket(SelectionKey sk, int event) {

        //创建一个
        SocketProcessor socketProcessor  = new SocketProcessor(sk,event,this.httpNioProtocolHandler);

        Executor.getExecutor().execute(socketProcessor);
        return  true;
    }

    /**
     *
     */
    protected class SocketProcessor implements Runnable{

        private SelectionKey sk;

        private int event;

        private HttpNioProtocolHandler httpNioProtocolHandler;

        public SocketProcessor(SelectionKey sk, int event,HttpNioProtocolHandler httpNioProtocolHandler) {
            this.sk = sk;
            this.event = event;
            this.httpNioProtocolHandler = httpNioProtocolHandler;
        }

        public void reset(SelectionKey sk, int event){
            this.sk = sk;
            this.event = event;
        }

        @Override
        public void run() {
            //避免读写并发运行
            synchronized (sk){
                //处理读请求
                if(this.event == SelectionKey.OP_READ){
                    try {
                        httpNioProtocolHandler.getHttp11Processor().service(sk);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void start() {

        this.running = true;
        //启动轮询器
        Thread thread = new Thread(poller);
        //设置执行线程名称
        thread.setName("poller-正在执行");
        thread.setPriority(5);
        thread.setDaemon(true);
        thread.start();

        //开启接受线程
        NIOEndPoint.this.startAcceptorThread();

        System.out.println("ip地址："+this.address+"端口号："+this.port+"======================>启动");
    }


    protected void startAcceptorThread() {

        acceptor = new Acceptor(this);

        String threadName =  "minicat-Acceptor-接受数据";
        Thread t = new Thread(acceptor, threadName);
        t.setPriority(5);
        t.setDaemon(true);
        t.start();
    }

    /**
     * 处理socket连接
     * @return
     * @param socket
     */
    public boolean setSocketOptions(SocketChannel socket){

        try{
            socket.configureBlocking(false);
            //处理socket 注册socket
            this.poller.register(socket);
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void stop() throws Exception {
        this.getServerSock().close();
    }

    public static void main(String[] args) throws Exception {
        NIOEndPoint nioEndPoint = new NIOEndPoint();
        nioEndPoint.init();
        nioEndPoint.start();
    }

}

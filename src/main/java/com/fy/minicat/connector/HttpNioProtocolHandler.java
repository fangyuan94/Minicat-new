package com.fy.minicat.connector;

/**
 * 转化器
 */
public class HttpNioProtocolHandler implements Container  {

    private Http11Processor http11Processor;

    private NIOEndPoint nioEndPoint;

    private FyAdapter fyAdapter;

    public void setFyAdapter(FyAdapter fyAdapter) {
        this.fyAdapter = fyAdapter;
    }

    public Http11Processor getHttp11Processor() {
        return http11Processor;
    }

    public NIOEndPoint getNioEndPoint() {
        return nioEndPoint;
    }



    @Override
    public void init() throws Exception {
        nioEndPoint =new NIOEndPoint();
        http11Processor = new Http11Processor();

        nioEndPoint.setHttpNioProtocolHandler(this);
        http11Processor.setAdapter(fyAdapter);
        nioEndPoint.init();
    }

    @Override
    public void start() {
        nioEndPoint.start();
    }

    @Override
    public void stop() throws Exception {
        nioEndPoint.stop();
    }
}

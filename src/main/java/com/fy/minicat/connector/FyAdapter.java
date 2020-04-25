package com.fy.minicat.connector;

import javax.servlet.http.HttpServletResponse;

/**
 * 适配器模式
 * 用于将内部req reps转化 为httpReq httpReps供Servlet使用
 * @author fangyuan
 */
public class FyAdapter  {

    private final Connector connector;

    public FyAdapter(Connector connector) {
        this.connector = connector;
    }

    public void service(Request request, Response response) throws Exception {

        //寻找对应的路径进行解析
        connector.getService().invok(request,response);
    }
}

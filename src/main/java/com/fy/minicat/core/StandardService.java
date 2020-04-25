package com.fy.minicat.core;

import com.fy.minicat.connector.Container;
import com.fy.minicat.connector.Request;
import com.fy.minicat.connector.Response;
import com.fy.minicat.mapper.Mapper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * 应用服务
 */
public class StandardService  {

    //整个服务中所有servlet
    private Mapper mapper;

    public void init(){
        mapper = new Mapper();
        mapper.init();
    }

    public void invok(Request request, Response response) throws Exception {

        //获取url
        String host = request.getRemoteHost();
        Mapper.MapperHost standardHost =  mapper.findHost(host);
        standardHost.invok(request,response);
    }

}

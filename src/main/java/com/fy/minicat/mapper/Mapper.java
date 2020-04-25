package com.fy.minicat.mapper;


import com.fy.minicat.connector.Http11Processor;
import com.fy.minicat.connector.Request;
import com.fy.minicat.connector.Response;
import com.fy.minicat.core.StandardContext;
import com.fy.minicat.core.StandardHost;
import com.fy.minicat.core.StandardWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServlet;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author
 */
public class Mapper {

    private MapperHost mapperHost;

    public MapperHost getMapperHost() {
        return mapperHost;
    }

    public void setMapperHost(MapperHost mapperHost) {
        this.mapperHost = mapperHost;
    }

    /**
     * 公共元素
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static  class MapperElement<T>{

        private String name;

        private T t;

    }

    @Getter
    @Setter
    public static final class MapperHost extends MapperElement<StandardHost>{

        private Map<String,MapperContext> contexts;

        public void invok(Request request, Response response) throws Exception {

            //找到context
            String url = request.getUrl();
            //先取第一个
            String[] urls = url.split("\\/");
            String u = "";
            for(int i=1;i<urls.length;i++){
                u+="/"+urls[i];
                if(contexts.containsKey(u)){
                    contexts.get(u).invok(request,response,this.getT().getAppBase(),url);
                    return;
                }
            }

            //没找到404处理 判断是否为静态资源
            response.outStatic(url);

        }

    }

    @Getter
    @Setter
    public static final class MapperContext extends MapperElement<StandardContext>{

        private Map<String,StandardWrapper> wrappers;

        public void invok(Request request, Response response,String base,String url) throws Exception {

            if(wrappers.containsKey(url)){

                wrappers.get(url).invok(request,response);
            }else {

                //可能请求如果请求静态资源 直接路由到静态资源
                response.outStatic(base+url);
            }
        }
    }


    /**
     * 读取server.xml配置文件
     */
    public  void init(){

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            //读取host
            Element elementHost = (Element) rootElement.selectSingleNode("//Host");

            String name = elementHost.attribute("name").getValue();

            String appBase = elementHost.attribute("appBase").getValue();

            Mapper mapper = new Mapper();

            //初始化 StandardHost
            StandardHost standardHost = new StandardHost(name,appBase);
            mapperHost = new MapperHost();
            mapperHost.setName(name);
            mapperHost.setT(standardHost);
            mapper.setMapperHost(mapperHost);

            List<Element> selectNodes = rootElement.selectNodes("//Context");
            Map<String,MapperContext> contexts = new HashMap<>(selectNodes.size());
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element =  selectNodes.get(i);
                String docBase = element.attribute("docBase").getValue();
                String path = element.attribute("path").getValue();
                StandardContext standardContext = new StandardContext(docBase,path);
                MapperContext mapperContext = new MapperContext();
                mapperContext.setT(standardContext);
                //读取各web xml下文件
                Map<String,StandardWrapper> standardWrappers =loadWebXMl(appBase,path);
                mapperContext.setWrappers(standardWrappers);
                contexts.put(path,mapperContext);
            }

            mapperHost.setContexts(contexts);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    private Map<String,StandardWrapper>  loadWebXMl(String appBase,String path){

        Map<String,StandardWrapper> standardWrappers = new HashMap<>();

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(appBase+"/"+path+"/web.xml");
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            List<Element> selectNodes = rootElement.selectNodes("//servlet");
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element =  selectNodes.get(i);
                // <servlet-name>lagou</servlet-name>
                Element servletnameElement = (Element) element.selectSingleNode("servlet-name");
                String servletName = servletnameElement.getStringValue();
                // <servlet-class>server.LagouServlet</servlet-class>
                Element servletclassElement = (Element) element.selectSingleNode("servlet-class");
                String servletClass = servletclassElement.getStringValue();

                // 根据servlet-name的值找到url-pattern
                Element servletMapping = (Element) rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
                // /lagou
                String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
                StandardWrapper standardWrapper  = new StandardWrapper();
                standardWrapper.setPattren(path+urlPattern);
                standardWrapper.setServlet((HttpServlet) Class.forName(servletClass).newInstance());
                standardWrappers.put(path+urlPattern,standardWrapper);
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return standardWrappers;
    }

    public MapperHost findHost(String url){

        return this.mapperHost;
    }



}

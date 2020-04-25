package com.fy.minicat.connector;

import java.io.IOException;

/**
 * @author fangyuan
 * 容器接口
 */
public interface Container {

    public void init() throws Exception;

    public void start()throws Exception;

    public void stop() throws Exception;
}

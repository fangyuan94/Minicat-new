package com.fy.minicat.core;

import lombok.Getter;
import lombok.Setter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 *针对
 */
@Getter
@Setter
public class StandardWrapper implements InvokInterface {

    private String pattren;

    private Servlet servlet;

    @Override
    public void invok(ServletRequest request, ServletResponse response) throws Exception {
        servlet.service(request,response);
    }
}

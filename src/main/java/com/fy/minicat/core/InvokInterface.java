package com.fy.minicat.core;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public interface InvokInterface {

    public void invok(ServletRequest request, ServletResponse response) throws Exception;

}

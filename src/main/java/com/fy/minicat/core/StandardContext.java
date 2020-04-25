package com.fy.minicat.core;

/**
 *应用属性
 */

public class StandardContext  {

    private String docBase;

    private String path;

    public StandardContext(String docBase, String path) {
        this.docBase = docBase;
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public String getPath() {
        return path;
    }

    public void invok() {

    }
}

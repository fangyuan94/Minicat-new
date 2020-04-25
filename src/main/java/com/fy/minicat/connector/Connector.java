package com.fy.minicat.connector;

import com.fy.minicat.core.StandardService;

public final class Connector {

    private StandardService service;

    private FyAdapter fyAdapter;

    private HttpNioProtocolHandler httpNioProtocolHandler;

    public FyAdapter getFyAdapter() {
        return fyAdapter;
    }

    public Connector(StandardService service) {
        this.service = service;
    }

    public StandardService getService() {
        return service;
    }

    public void init() throws Exception {
        httpNioProtocolHandler = new HttpNioProtocolHandler();
        fyAdapter = new FyAdapter(this);
        httpNioProtocolHandler.setFyAdapter(fyAdapter);
        httpNioProtocolHandler.init();

    }

    public void start() throws Exception {
        httpNioProtocolHandler.start();
    }


}

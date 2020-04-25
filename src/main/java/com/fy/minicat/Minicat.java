package com.fy.minicat;

import com.fy.minicat.connector.Connector;
import com.fy.minicat.core.StandardService;

/**
 *
 */
public class Minicat {

    private Connector connector;

    public Minicat(){

    }

    public void init(){
        StandardService standardService = new StandardService();
        connector = new Connector(standardService);
        try {
            standardService.init();
            connector.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void start(){

        try {
            connector.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){

    }



}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;

import java.util.concurrent.RecursiveAction;

/**
 * @author fs
 */
public class DataPreload extends RecursiveAction {

    private JEVisDataSource ds;
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(DataPreload.class);

    public DataPreload(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    protected void compute() {
        try {
            this.ds.preload();//will load all classes
        } catch (JEVisException ex) {
            logger.catching(ex);
        }

    }

}

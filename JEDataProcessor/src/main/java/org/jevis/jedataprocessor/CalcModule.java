/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor;

import com.google.inject.AbstractModule;
import org.jevis.api.JEVisDataSource;

/**
 * @author broder
 */
public class CalcModule extends AbstractModule {

    private final JEVisDataSource datasource;

    public CalcModule(JEVisDataSource dataSource) {
        this.datasource = dataSource;
    }

    @Override
    protected void configure() {
    }
}

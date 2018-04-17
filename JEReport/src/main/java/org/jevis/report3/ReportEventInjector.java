/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.jevis.report3.data.report.Finisher;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.Precondition;
import org.jevis.report3.data.report.ReportExecutor;
import org.jevis.report3.data.report.event.EventFinisher;
import org.jevis.report3.data.report.event.EventIntervalCalc;
import org.jevis.report3.data.report.event.EventPrecondition;

/**
 *
 * @author broder
 */
class ReportEventInjector extends AbstractModule {

    @Override
    protected void configure() {
        bind(Precondition.class).to(EventPrecondition.class);
        bind(Finisher.class).to(EventFinisher.class);
        bind(IntervalCalculator.class).to(EventIntervalCalc.class);
        install(new FactoryModuleBuilder().implement(ReportExecutor.class, ReportExecutor.class)
                .build(ReportExecutorFactory.class));
    }

}

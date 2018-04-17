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
import org.jevis.report3.data.report.periodic.PeriodPrecondition;
import org.jevis.report3.data.report.Precondition;
import org.jevis.report3.data.report.ReportExecutor;
import org.jevis.report3.data.report.periodic.PeriodFinisher;
import org.jevis.report3.data.report.periodic.PeriodicIntervalCalc;

/**
 *
 * @author broder
 */
public class ReportPeriodicInjector extends AbstractModule {

    @Override
    protected void configure() {
        bind(Precondition.class).to(PeriodPrecondition.class);
        bind(Finisher.class).to(PeriodFinisher.class);
        bind(IntervalCalculator.class).to(PeriodicIntervalCalc.class);
        install(new FactoryModuleBuilder().implement(ReportExecutor.class, ReportExecutor.class)
                .build(ReportExecutorFactory.class));
    }

}

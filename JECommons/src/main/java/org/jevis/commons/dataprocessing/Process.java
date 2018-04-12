/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing;

import java.util.List;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface Process {

    /**
     * Set the JEVisDatasource
     *
     * @param ds
     */
    void setJEVisDataSource(JEVisDataSource ds);

    /**
     * Returns the JEVisDatasource
     *
     * @return
     */
    JEVisDataSource getJEVisDataSource();

    /**
     * Set the ID of this Task. The ID should be Unique unter the parent task.
     *
     * @param id
     */
    void setID(String id);

    /**
     * Retuns the ID of this Task. The ID should be Unique unter the parent
     * task.
     *
     * @return
     */
    String getID();

    /**
     * Returns an list of all Options for this task
     *
     * @return
     */
    List<ProcessOption> getOptions();

    /**
     * Returns the processor type.
     *
     * @return
     */
    ProcessFunction getFunction();

    /**
     * Set The processor used to this task
     *
     * @param processor
     */
    void setFunction(ProcessFunction processor);

    /**
     * Set the options for this task
     *
     * @param options
     */
    void setOptions(List<ProcessOption> options);

    /**
     * returns the result of this task
     *
     * @return
     */
    List<JEVisSample> getResult();

    void setSubProcesses(List<Process> processes);

    List<Process> getSubProcesses();

    public void setObject(JEVisObject object);

    public JEVisObject getObject();

    public void restResult();

    public Process getParent();

    /**
     * Note will not set the child in the parent....
     *
     * @param parent
     */
    public void setParent(Process parent);

//    public Task setParent(Task parent);
}

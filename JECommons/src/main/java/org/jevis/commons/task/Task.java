package org.jevis.commons.task;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

public interface Task {

    Long getID();

    Status getStatus();

    void setStatus(Status status);

    String getTaskName();

    void setTaskName(String name);

    DateTime getStartTime();

    String getErrors();

    Period getRunTime();

    Exception getException();

    void setException(Exception exception);

    List<TaskStep> getSteps();

    void addStep(TaskStep step);

    void addStep(String type, Object message);


    DateTime getEndTime();

    enum Status {UNKNOWN, IDLE, STOPPED, STARTED, SCHEDULED, RUNNING, FINISHED, FAILED}

}

package org.jevis.commons.task;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class CommonTask implements Task {

    private final Long taskID;
    private Status status = Status.UNKNOWN;
    private DateTime startDate;
    private DateTime endDate;
    private String name = "";
    private String error;
    private Exception exception;
    private final List<TaskStep> steps = new ArrayList<>();

    public CommonTask(Long id) {
        taskID = id;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;

        if (status == Status.STARTED) {
            startDate = new DateTime();
        } else if (status == Status.FINISHED || status == Status.FAILED || status == Status.STOPPED) {
            endDate = new DateTime();
        }


    }

    @Override
    public Long getID() {
        return taskID;
    }

    @Override
    public Period getRunTime() {
        try {
            if (startDate == null) {
                return Period.ZERO;
            } else if (endDate == null) {
                Period period = new Period(startDate, (new DateTime()));
                return period;
            } else {
                Period period = new Period(startDate, endDate);
                return period;
            }
        } catch (Exception ex) {
            return Period.ZERO;
        }


    }

    @Override
    public String getTaskName() {
        return name;
    }

    @Override
    public void setTaskName(String name) {
        this.name = name;
    }

    @Override
    public DateTime getStartTime() {
        return startDate;
    }

    @Override
    public String getErrors() {
        return error;
    }

    @Override
    public Exception getException() {
        //setStatus(Status.FAILED);
        return exception;
    }

    @Override
    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public List<TaskStep> getSteps() {
        return steps;
    }

    @Override
    public void addStep(TaskStep step) {
        steps.add(step);
    }

    @Override
    public void addStep(String type, Object message) {
        steps.add(new TaskStep(type, message));
    }

    @Override
    public DateTime getEndTime() {
        return endDate;
    }
}

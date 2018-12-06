package org.jevis.commons.task;

import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogTaskManager {


    private static LogTaskManager taskManager;

    private Map<Long, Task> tasks = new ConcurrentHashMap<>();

    private LogTaskManager() {
    }

    public static LogTaskManager getInstance() {
        if (taskManager == null) {
            taskManager = new LogTaskManager();
        }

        return taskManager;
    }

    public static String parentName(JEVisObject obj) {
        try {
            if (obj != null && obj.getParents() != null) {
                return obj.getParents().get(0).getName();
            }
        } catch (Exception ex) {

        }
        return "-";
    }

    public Task buildNewTask(long id, String name) {
        Task newTask = new CommonTask(id);
        newTask.setTaskName(name);
        tasks.put(newTask.getID(), newTask);
        return newTask;
    }

    public Task buildNewTask(long id) {
        return buildNewTask(id, "");
    }

    public Task getTask(long id) {
        if (!tasks.containsKey(id)) {
            tasks.put(id, new CommonTask(id));
        }
        return tasks.get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void clearWithStatus(Task.Status status) {
        List<Task> toRemove = new ArrayList<>();
        tasks.forEach((aLong, task) -> {
            if (task.getStatus() == status) {
                toRemove.add(task);
            }
        });

        toRemove.forEach(task -> {
            tasks.remove(task);
        });

    }

    public String getShortErrorMessage(Exception ex) {
//        System.out.println("getShortErrorMessage: " + ex);
        try {
            for (StackTraceElement te : ex.getStackTrace()) {
                if (te.getClassName().startsWith("org.jevis")) {
                    String shortClassName = "";
                    if (te.getClassName().lastIndexOf(".") != -1) {
                        shortClassName = te.getClassName().substring(te.getClassName().lastIndexOf(".") + 1);
                    } else {
                        shortClassName = te.getClassName();
                    }
                    return shortClassName + ":" + te.getLineNumber() + ":" + te.getMethodName();
                }
            }
            return ex.toString();
        } catch (Exception exp2) {
            return ex.toString();
        }

    }
}

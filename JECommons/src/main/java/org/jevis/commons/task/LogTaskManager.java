package org.jevis.commons.task;

import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogTaskManager {


    private static LogTaskManager taskManager;

    private Map<Long, Task> tasks = new HashMap<>();

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
}

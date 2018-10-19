package org.jevis.commons.task;

import dnl.utils.text.table.TextTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import java.util.*;

public class TaskPrinter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private static final Logger logger = LogManager.getLogger(TaskPrinter.class);

    public static void printJobStatus(LogTaskManager taskManager) {
        List<Task> allTasks = taskManager.getAllTasks();
        Map<String, Integer> stepHeader = new HashMap<>();

        try {
            int nextInt = 5;
            /**
             * Find all used Steps and give them an header number
             */
            for (Task task : allTasks) {
                for (TaskStep step : task.getSteps()) {
//                    System.out.println("Step: " + step.getType());
                    if (!stepHeader.containsKey(step.getType())) {
                        System.out.println("add step: " + nextInt + " " + step.getType());
                        stepHeader.put(step.getType(), nextInt);
                        nextInt++;
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Object[][] data = new Object[allTasks.size()][5 + stepHeader.size()];

        for (int i = 0; i < allTasks.size(); i++) {
            Task task = allTasks.get(i);
            data[i][0] = task.getID();
            data[i][1] = task.getTaskName().length() < 40 ? task.getTaskName() : task.getTaskName().substring(0, 40) + "..";
            data[i][2] = task.getStatus();
            data[i][3] = FORMATTER.print(task.getStartTime());
            data[i][4] = task.getRunTime().toPeriod().getSeconds();
            data[i][4] = task.getRunTime().toPeriod().getMillis() < 1000
                    ? task.getRunTime().toPeriod().getMillis() + "msec"
                    : task.getRunTime().toPeriod().getSeconds() + "sec";

            //Dynamic Steps info
            for (TaskStep tStep : task.getSteps()) {
                data[i][stepHeader.get(tStep.getType())] = tStep.getMessage();
            }


        }

        List<String> columns = new ArrayList<>();
        columns.add("JEVisID");
        columns.add("Name");
        columns.add("Status");
        columns.add("Job Started ");
        columns.add("Runtime");
        List<Integer> hTemp = new ArrayList<>(stepHeader.values());

        Collections.sort(hTemp);
        hTemp.forEach(integer -> {
            stepHeader.forEach((key, value) -> {
                if (value.equals(integer)) {
                    columns.add(key);
                }
            });
        });


        TextTable table = new TextTable(columns.toArray(new String[0]), data);
        table.setAddRowNumbering(true);
        table.setSort(1, SortOrder.DESCENDING);
        table.printTable();
    }


}

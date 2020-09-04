package org.jevis.commons.task;

import dnl.utils.text.table.TextTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TaskPrinter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yy-MM-dd HH:mm");//yyyy-MM-dd HH:mm
    private static final Logger logger = LogManager.getLogger(TaskPrinter.class);

    public static void printJobStatus(LogTaskManager taskManager) {
        List<Task> allTasks = taskManager.getAllTasks();
        Map<String, Integer> stepHeader = new HashMap<>();

        try {
            int nextInt = 7;
            /**
             * Find all used Steps and give them an header number
             */
            for (Task task : allTasks) {
                for (TaskStep step : task.getSteps()) {
//                    System.out.println("Step: " + step.getType());
                    if (!stepHeader.containsKey(step.getType())) {
                        stepHeader.put(step.getType(), nextInt);
                        nextInt++;
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Object[][] data = new Object[allTasks.size()][7 + stepHeader.size()];

        if (allTasks.size() > 0) {
            for (int i = 0; i < allTasks.size(); i++) {
                Task task = allTasks.get(i);
                data[i][0] = task.getID();
                data[i][1] = task.getTaskName().length() < 40 ? task.getTaskName() : task.getTaskName().substring(0, 40) + "..";
                data[i][2] = task.getStatus();
                data[i][3] = FORMATTER.print(task.getStartTime());

                if (task.getRunTime().toStandardDuration().getMillis() < 1000)
                    data[i][4] = task.getRunTime().toStandardDuration().getMillis() + " msec";
                else data[i][4] = task.getRunTime().toStandardDuration().getStandardSeconds() + "  sec";

                String shortError = "";
                if (task.getException() != null) {
                    try {
                        shortError = task.getException().getStackTrace()[0].getClassName().substring(
                                task.getException().getStackTrace()[0].getClassName().lastIndexOf(".") + 1
                        )
                                + ":" + task.getException().getStackTrace()[0].getLineNumber()
                                + ":" + task.getException().getStackTrace()[0].getMethodName();


                        shortError = shortError.length() < 50
                                ? shortError
                                : ".." + shortError.substring(shortError.length() - 50);

                    } catch (Exception ex) {
                    }
                }

                data[i][5] = shortError;

                data[i][6] = task.getID();

                //Dynamic Steps info
                for (TaskStep tStep : task.getSteps()) {
                    data[i][stepHeader.get(tStep.getType())] = tStep.getMessage();
                }


            }
        }

        List<String> columns = new ArrayList<>();
        columns.add("JEVisID");
        columns.add("Name");
        columns.add("Status");
        columns.add("Job Started ");
        columns.add("Runtime");
        columns.add("Error Location");
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
        table.setSort(2, SortOrder.ASCENDING);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, "utf8")) {
            table.printTable(ps, 0);
            logger.error("Status Table\n" + new String(baos.toByteArray(), StandardCharsets.UTF_8));
        } catch (Exception ex) {

        }
//        table.printTable();
    }


}

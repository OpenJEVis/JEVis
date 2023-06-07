package org.jevis.jecc.application.jevistree.filter;

import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;

import java.util.ArrayList;
import java.util.List;

public class DirectoryHelper {

    private static DirectoryHelper instance;
    private final JEVisDataSource dataSource;
    private List<JEVisClass> directories;
    private List<String> directoriesNames;

    private DirectoryHelper(JEVisDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static DirectoryHelper getInstance(JEVisDataSource ds) {

        if (DirectoryHelper.instance == null) {
            DirectoryHelper.instance = new DirectoryHelper(ds);
        }
        return DirectoryHelper.instance;
    }

    public List<String> getDirectoryNames() throws JEVisException {
        if (directories == null) {
            JEVisClass directoryClass = dataSource.getJEVisClass("Directory");
            directories = directoryClass.getHeirs();
            directoriesNames = new ArrayList<>();
            directories.forEach(jeVisClass -> {
                try {
                    directoriesNames.add(jeVisClass.getName());
                } catch (Exception ex) {
                }
            });
        }

        return directoriesNames;
    }
}

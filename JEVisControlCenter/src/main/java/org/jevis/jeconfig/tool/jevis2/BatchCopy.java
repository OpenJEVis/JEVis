package org.jevis.jeconfig.tool.jevis2;

import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.jevistree.TreeHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BatchCopy {


    //blind 8681
    //Wirk 8683


    public BatchCopy(File file, JEVisObject parentObject, JEVisDataSource ds) throws FileNotFoundException, JEVisException {

        JEVisObject blindTemplate = ds.getObject(8681l);
        JEVisObject wirkTemplate = ds.getObject(8683l);

        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.isEmpty()) continue;

            System.out.println("Create: " + line);
            JEVisObject template;
            if (line.toLowerCase().contains("blind") || line.toLowerCase().contains("schein")) {
                template = blindTemplate;
            } else {
                template = wirkTemplate;
            }


            TreeHelper.copyObject(template, parentObject, line, true, true);


        }
    }
}

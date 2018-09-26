/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;

import java.io.*;

/**
 * @author fs
 */
public class JsonFileExporter {
    private static final Logger logger = LogManager.getLogger(JsonFileExporter.class);

    public static void writeToFile(File file, JsonObject obj) {

//        Gson gson = new Gson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(obj);

        try {
            //write converted json data to a file named "file.json"
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();

        } catch (IOException e) {
            logger.fatal(e);
        }

    }

    public static JEVisObject loadFromFile(File file) {
        Gson gson = new Gson();

        try {

            BufferedReader br = new BufferedReader(
                    new FileReader(file));

            //convert the json string back to object
            JsonObject obj = gson.fromJson(br, JsonObject.class);

            logger.info(obj);

        } catch (IOException e) {
            logger.fatal(e);
        }

        return null;
    }

}

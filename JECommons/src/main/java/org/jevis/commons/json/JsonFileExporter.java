/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.jevis.api.JEVisObject;

/**
 *
 * @author fs
 */
public class JsonFileExporter {

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
            e.printStackTrace();
        }

    }

    public static JEVisObject loadFromFile(File file) {
        Gson gson = new Gson();

        try {

            BufferedReader br = new BufferedReader(
                    new FileReader(file));

            //convert the json string back to object
            JsonObject obj = gson.fromJson(br, JsonObject.class);

            System.out.println(obj);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}

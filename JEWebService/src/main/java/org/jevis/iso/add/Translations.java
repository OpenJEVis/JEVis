/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.jevis.iso.rest.Login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Translations {

    public List<FormAttribute> translate(List<FormAttribute> listfa, String language) throws Exception {
        for (FormAttribute fa : listfa) {
            InputStream is = getResourceAsStream("/lang/" + language + ".json");
            String jsonTxt = IOUtils.toString(is, "UTF-8");

            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonTxt).getAsJsonObject();

            String att = fa.getName();

            if (Objects.nonNull(json.get(fa.getName()))) {
                att = json.get(fa.getName()).getAsString();
            }

            fa.setTransname(att);
        }
        return listfa;
    }

    public String getTranslatedKey(String language, String keyName) throws IOException {
        String output = "";
        InputStream is = getResourceAsStream("/lang/" + language + ".json");
        String jsonTxt = IOUtils.toString(is, "UTF-8");

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonTxt).getAsJsonObject();
        if (Objects.nonNull(json.get(keyName))) {
            output = json.get(keyName).getAsString();
        }
        return output;
    }

    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
//        final InputStream in = getContextClassLoader().getResourceAsStream(resource);
//
//        return in == null ? getClass().getResourceAsStream(resource) : in;
        return Login.class.getResourceAsStream(resource);
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

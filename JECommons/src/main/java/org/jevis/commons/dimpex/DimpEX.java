package org.jevis.commons.dimpex;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class DimpEX {

    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(DimpEX.class);


    public static DimpexObject export(JEVisObject exObj, boolean includeChildren, DimpExfactory.SampleMode smode) throws JEVisException {
        DimpexObject json = DimpExfactory.build(exObj, includeChildren, smode);
        if (includeChildren) {
            List<DimpexObject> jsons = new ArrayList<>();
            for (JEVisObject obj : exObj.getChildren()) {
                try {
                    DimpexObject json2 = export(obj, includeChildren, smode);
                    jsons.add(json2);
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
            json.setChildren(jsons);
        }
        return json;
    }

    public static List<DimpexObject> exportAll(List<JEVisObject> objects, boolean includeChildren, DimpExfactory.SampleMode smode) {
        List<DimpexObject> jsons = new ArrayList<>();
        for (JEVisObject obj : objects) {
            try {
                DimpexObject json = export(obj, includeChildren, smode);
                jsons.add(json);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return jsons;
    }


    public static List<DimpexObject> readFile(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        ObjectMapper objectMapper = new ObjectMapper();

//        Gson gson = new Gson();
//        Type listType = new TypeToken<List<DimpexObject>>() {
//        }.getType();
        try {
//            List<DimpexObject> objetcs = gson.fromJson(bufferedReader, listType);

            return Arrays.asList(objectMapper.readValue(bufferedReader, DimpexObject[].class));
        } catch (JsonParseException | JsonMappingException ie) {
            DimpexObject object = objectMapper.readValue(bufferedReader, DimpexObject.class);
            List<DimpexObject> objects = new ArrayList<>();
            objects.add(object);
            return objects;
        }

    }

    public static void importALL(JEVisDataSource ds, List<DimpexObject> objects, JEVisObject parent) {
        for (DimpexObject obj : objects) {
            try {
                JEVisClass jclass = ds.getJEVisClass(obj.getJclass());
                if (jclass == null) {
                    logger.error("class does not exists: " + obj.getJclass());
                    continue;
                }
                if (!jclass.isAllowedUnder(parent.getJEVisClass())) {
                    logger.error("class is not allowed under parent: " + obj.getJclass());
                    continue;
                }

                JEVisObject newObject = parent.buildObject(obj.getName(), jclass);
                newObject.commit();
                logger.info("New ID: " + newObject.getID());

                if (obj.getChildren() != null) {
                    importALL(ds, obj.getChildren(), newObject);
                }


                if (obj.getAttributes() != null) {
                    for (DimpexAttribute datt : obj.getAttributes()) {
                        try {
                            JEVisAttribute att = newObject.getAttribute(datt.getName());
                            if (att == null) {
                                logger.warn("Attribute does not exists: " + datt.getName());
                                continue;
                            }
                            att.setDisplayUnit(new JEVisUnitImp(datt.getDisplayUnit()));
                            att.setInputUnit(new JEVisUnitImp(datt.getInputUnit()));
                            att.setDisplaySampleRate(Period.parse(datt.getDisplayRate()));
                            att.setInputSampleRate(Period.parse(datt.getInputRate()));
                            att.commit();

                            if (datt.getSamples() != null) {
                                List<JEVisSample> samples = new ArrayList<>();
                                for (JsonSample sample : datt.getSamples()) {
                                    DateTime ts = JsonFactory.sampleDTF.parseDateTime(sample.getTs());
                                    JEVisSample newSample = att.buildSample(ts, sample.getValue(), sample.getNote());
                                    samples.add(newSample);
                                }
                                att.addSamples(samples);
                            }

                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                }


            } catch (Exception ex) {
                logger.error(ex);
            }
        }


    }

    public static void writeFile(DimpexObject objects, File output, boolean gzip) throws IOException {
        List<DimpexObject> jsons = new ArrayList<>();
        jsons.add(objects);
        writeFile(jsons, output, gzip);
    }

    public static void writeFile(List<DimpexObject> objects, File output, boolean gzip) throws IOException {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try (FileOutputStream fos = new FileOutputStream(output);
             OutputStreamWriter isr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {


            objectMapper.writeValue(isr, objects);

            if (gzip) {
                gzipIt(output.getName(), output.toString() + ".zip");
            }
        }
    }

    public static void gzipIt(String SOURCE_FILE, String OUTPUT_GZIP_FILE) {

        byte[] buffer = new byte[1024];

        try {

            GZIPOutputStream gzos =
                    new GZIPOutputStream(new FileOutputStream(OUTPUT_GZIP_FILE));

            FileInputStream in =
                    new FileInputStream(SOURCE_FILE);

            int len;
            while ((len = in.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }

            in.close();

            gzos.finish();
            gzos.close();

            logger.info("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}

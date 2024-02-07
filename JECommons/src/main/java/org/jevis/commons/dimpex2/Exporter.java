package org.jevis.commons.dimpex2;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.math3.analysis.function.Exp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisEvent;
import org.jevis.api.JEVisEventListener;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Exporter {

    private final EventListenerList listeners = new EventListenerList();
    private static final Logger logger = LogManager.getLogger(Exporter.class);

    //Take JEVis Object
    //Generate pojo
    //get children list, if selected
    //generate one file per child
    //get Sample for all attributes, if selected (Values !=sample=two options)
    ObjectMapper objectMapper = new ObjectMapper();

    public Exporter(File tmpDir, JEVisObject jeVisObject) throws IOException {

        File zipFileName = new File("");

        List<ObjectNode> nodes = new ArrayList<>();

        jeVisObject.getChildren()
        nodes.forEach(jsonNodes -> {
            notifyListeners(new DimpExEvent(this, DimpExEvent.TYPE.OBJECT_TMPFILE_CREATE,jsonNodes,"Create TMP file: "+jsonNodes.asText()));
            File file = new File(tmpDir.getAbsolutePath()+File.separator+);
            objectMapper.writer(new DefaultPrettyPrinter());
            objectMapper.writeValue(new File(tmpDir.getAbsolutePath()+"D:/cp/dataTwo.json"), jsonDataObject);
        });


        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {


        }
    }

    private void exportJEVIsPath(File zipFile, JEVisObject rootObj) throws IOException {

        File systemTmpDir = new File(System.getProperty("java.io.tmpdir"));
        File zipTmpDir = new File(System.getProperty("java.io.tmpdir")+File.pathSeparator+UUID.randomUUID());
        zipTmpDir.mkdirs();
        zipTmpDir.deleteOnExit();

        List<ExportSetting> exportSettingList = new ArrayList<>();
        exportSettingList.add(new ExportSetting(rootObj));

        try {
            walkTree(rootObj, exportSettingList);
        }catch (Exception ex){
            logger.error(ex,ex);
        }




        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile))) {

            exportSettingList.forEach(exportSetting -> {
                FileInputStream inputStream = new FileInputStream(zipTmpDir+);
                ZipEntry zipEintrag = new ZipEntry(datei.getName());
                zipStream.putNextEntry(zipEintrag);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    zipStream.write(buffer, 0, bytesRead);
                }
            });



        }

        //Delete all files


    }

    private static void walkTree(JEVisObject jeVisObject, List<ExportSetting> fileList) throws JEVisException {

        jeVisObject.getChildren().forEach(child -> {
            try {
                ExportSetting setting = new ExportSetting();
                fileList.add(setting);
                walkTree(child, fileList);
            }catch (Exception ex){
                logger.error(ex,ex);
            }
        });
    }

    private void createTmpFiles(File tmpDir, List<ExportSetting> objects){
        if(!tmpDir.exists()) tmpDir.mkdirs();

        objects.forEach(exportSetting -> {
            try {
                ObjectNode objectNode = objectToNode(exportSetting.getObject());
                File file = new File(tmpDir + File.separator + exportSetting.getFilename());
                objectMapper.writeValue(file, objectNode);
            }catch (Exception ex){
                logger.error(ex,ex);
            }
        });
    }


    private File toFile(JEVisObject object) {
        return null;
    }

    //joe-ren

    private ObjectNode objectToNode(JEVisObject object) {


        UUID uuid = UUID.randomUUID();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode
                .put(JsonNames.Object.NAME, object.getName())
                .put(JsonNames.Object.REF, uuid.toString());


        return objectNode;

    }

    public void addEventListener(DimpExEventListener listener) {
        if (this.listeners.getListeners(JEVisEventListener.class).length > 0) {
            logger.debug("Duplicate Listener: {}", "");
        }

        this.listeners.add(DimpExEventListener.class, listener);
    }

    public void removeEventListener(DimpExEventListener listener) {
        this.listeners.remove(DimpExEventListener.class, listener);
    }

    public synchronized void notifyListeners(DimpExEvent event) {
        logger.trace("Object event[{}] listeners: {} event:",  this.listeners.getListenerCount(), event.getType());
        for (DimpExEventListener l : this.listeners.getListeners(DimpExEventListener.class)) {
            l.fireEvent(event);
        }
    }

    public DimpExEventListener[] getEventListener() {
        return this.listeners.getListeners(DimpExEventListener.class);
    }
}

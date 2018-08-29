package org.jevis.jeconfig.plugin.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jevis.api.*;
import org.jevis.commons.utils.FileIcon;
import org.jevis.jeconfig.plugin.dashboard.data.ScadaAnalysisData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SCADAAnalysis {

    private String title = "";
    private Image image;
    private boolean fullScreenBackground = true;
    private Color backgroundColor = Color.WHITE;
    private Color fontColor = Color.BLACK;
    private List<SCADAElement> elements = new ArrayList<>();
    private BGMode backgroundMode = BGMode.STRETCH_BOTH;
    private JEVisObject jevisObject;


    public SCADAAnalysis(JEVisObject obj) {
        this.jevisObject = obj;
    }


    public BGMode getBackgroundMode() {
        return backgroundMode;
    }

    public void setBackgroundMode(BGMode backgroundMode) {
        this.backgroundMode = backgroundMode;
    }

    public JEVisObject getObject() {
        return jevisObject;
    }

    public void load(JEVisObject analysisObj) throws JEVisException {

        title = analysisObj.getName();


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {

            JEVisAttribute dataModel = analysisObj.getAttribute("Model Data");
            JEVisSample dmSample = dataModel.getLatestSample();
            if (dmSample != null && !dmSample.getValueAsString().isEmpty()) {
                ScadaAnalysisData jsonData = gson.fromJson(dmSample.getValueAsString(), ScadaAnalysisData.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        //load json
        JEVisAttribute file = analysisObj.getAttribute("Background");
        JEVisSample backgroundImage = file.getLatestSample();

        if (backgroundImage != null) {
            try {
                image = SwingFXUtils.toFXImage(FileIcon.getIcon(backgroundImage.getValueAsFile()), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        JEVisDataSource ds = analysisObj.getDataSource();


        //fake load for prototype
        SCADAElement ele1 = new LabelElement(this);
        ele1.setAttribute(ds.getObject(8742l).getAttribute("Value"));
//        ele1.heightProperty().set(50);
//        ele1.widthProperty().set(80);
        ele1.xPositionProperty().setValue(205);
        ele1.yPositionProperty().setValue(100);
        ele1.titleProperty().setValue("Label Nr.1:");


//        SCADAElement ele2 = new LabelElement();
//        ele2.setAttribute(ds.getObject(8743l).getAttribute("Value"));

        elements.add(ele1);
//        elements.add(ele2);


    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public boolean isFullScreenBackground() {
        return fullScreenBackground;
    }

    public void setFullScreenBackground(boolean fullScreenBackground) {
        this.fullScreenBackground = fullScreenBackground;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }


    public List<SCADAElement> getElements() {
        return elements;
    }

    public void setElements(List<SCADAElement> elements) {
        this.elements = elements;
    }

    enum BGMode {
        ABSOLUTE, STRETCH_BOTH, STRETCH_HEIGHT, STRETCH_WIDTH
    }


}

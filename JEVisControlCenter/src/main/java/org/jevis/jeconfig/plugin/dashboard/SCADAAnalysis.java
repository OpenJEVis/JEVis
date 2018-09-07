package org.jevis.jeconfig.plugin.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.paint.Color;
import org.jevis.api.*;
import org.jevis.jeconfig.plugin.dashboard.data.ScadaAnalysisData;
import org.jevis.jeconfig.plugin.dashboard.data.ScadaElementData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SCADAAnalysis {

    private String title = "";
    private BufferedImage image2;
    private boolean fullScreenBackground = true;
    private Color backgroundColor = Color.WHITE;
    private Color fontColor = Color.BLACK;
    private List<SCADAElement> elements = new ArrayList<>();
    private BGMode backgroundMode = BGMode.STRETCH_BOTH;
    private JEVisObject jevisObject;
    private boolean bgChanged = false;

    public SCADAAnalysis(JEVisObject obj) {
        this.jevisObject = obj;
    }

    public boolean bgHasChanged() {
        return bgChanged;
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

    public void setObject(JEVisObject obj) {
        this.jevisObject = obj;
    }

    //    public void load(JEVisObject analysisObj) throws JEVisException {
    public void load() throws JEVisException {
        System.out.println("==SCADA Analysis.load()==");
        if (jevisObject == null) {
            return;
        }

        title = jevisObject.getName();


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {

            JEVisAttribute dataModel = jevisObject.getAttribute(SCADAPlugin.ATTRIBUTE_DATA_MODEL);
            JEVisSample dmSample = dataModel.getLatestSample();
            if (dmSample != null && !dmSample.getValueAsString().isEmpty()) {

                ScadaAnalysisData jsonData = gson.fromJson(dmSample.getValueAsString(), ScadaAnalysisData.class);
                jsonData.getElements().forEach(scadaElementData -> {
                    System.out.println("Add element: " + scadaElementData.getType());
                    elements.add(getElement(scadaElementData));
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        //load json
        JEVisAttribute file = jevisObject.getAttribute(SCADAPlugin.ATTRIBUTE_BACKGROUND);
        JEVisSample backgroundImage = file.getLatestSample();

        if (backgroundImage != null) {
            try {
                JEVisFile bgFile = backgroundImage.getValueAsFile();
                //png
                InputStream in = new ByteArrayInputStream(bgFile.getBytes());
                image2 = ImageIO.read(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private SCADAElement getElement(ScadaElementData data) {
        SCADAElement ele;
        if (data.getType().equals(LabelElement.TYPE)) {
            ele = new LabelElement(this);
        } else {
            /** fallback **/
            ele = new LabelElement(this);
        }
        ele.setData(data);

        return ele;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BufferedImage getImage() {
        if (image2 == null) {
            System.out.println("no Image set using fallback");
            return new BufferedImage(1, 1, 1);
        }
        return image2;
    }


    public void setImage(BufferedImage image) {
        this.bgChanged = true;
        this.image2 = image;
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

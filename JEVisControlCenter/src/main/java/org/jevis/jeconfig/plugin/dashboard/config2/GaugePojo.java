package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

import java.util.ArrayList;
import java.util.List;

public class GaugePojo {




    private static final Logger logger = LogManager.getLogger(GaugePojo.class);

    int gaugeWidgetID = -1;

    private double maximum = 50;

    private double minimum = 0;

    private boolean inPercent = false;

    private List<GaugeSectionPojo> sections = new ArrayList();

    final DashboardControl dashboardControl;


    ObservableList<String> skins = FXCollections.observableArrayList();

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }



    public GaugePojo(DashboardControl control) {
        this(control, null);
    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
    }

    public GaugePojo(DashboardControl control, JsonNode jsonNode) {

        //gauge.setSkinType(Gauge.SkinType.SIMPLE);
        skins.addAll(eu.hansolo.medusa.Gauge.SkinType.DASHBOARD.toString(), eu.hansolo.medusa.Gauge.SkinType.SIMPLE.toString());
        this.dashboardControl = control;

        if (jsonNode != null) {

            maximum = jsonNode.get("maximum").asDouble();
            minimum = jsonNode.get("minimum").asDouble();
            inPercent = jsonNode.get("inPercent").asBoolean();
            for (int i = 0; i < jsonNode.get("sections").size(); i++) {
                double sectionEnd = jsonNode.get("sections").get(i).get("end").asDouble();
                double sectionStart = jsonNode.get("sections").get(i).get("start").asDouble();
                Color sectionColor = Color.valueOf(jsonNode.get("sections").get(i).get("color").asText());
                sections.add(new GaugeSectionPojo(sectionStart, sectionEnd, sectionColor));
            }




        }


    }


    public List<GaugeSectionPojo> getSections() {
        return sections;
    }

    public void setSections(List<GaugeSectionPojo> sections) {
        this.sections = sections;
    }


    public boolean isInPercent() {
        return inPercent;
    }

    public void setInPercent(boolean inPercent) {
        this.inPercent = inPercent;
    }

    @Override
    public String toString() {
        return "Gauge{" +
                "gaugeWidgetID=" + gaugeWidgetID +
                ", maximum=" + maximum +
                ", minimum=" + minimum +
                ", inPercent=" + inPercent +
                ", sections=" + sections +
                ", dashboardControl=" + dashboardControl +
                ", skins=" + skins +
                '}';
    }


    private class GaugeDesignTab extends Tab implements ConfigTab {
        GaugePojo gaugeDesign;

        public GaugeDesignTab(String text, GaugePojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {
            System.out.println("this");
        }
    }

    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.gaugewidget.tab")
                , this);
        ScrollPane scrollPane = new ScrollPane();
        VBox vBox = new VBox();
        vBox.setSpacing(8);
        vBox.getChildren().add(createMaxMin());

        JFXButton buttonAddSection = new JFXButton(I18n.getInstance().getString("plugin.dashboard.gaugewidget.section.add"));
        JFXButton buttonRemoveSection = new JFXButton(I18n.getInstance().getString("plugin.dashboard.gaugewidget.section.remove"));




        vBox.getChildren().add(new HBox(8,buttonAddSection,buttonRemoveSection));
        for (GaugeSectionPojo section:sections) {
            VBox vBoxSection = createNewSection(section);
            vBox.getChildren().add(vBox.getChildren().size()-1,vBoxSection);
        }


        buttonAddSection.setOnAction(event -> {
            sections.add(new GaugeSectionPojo(0.0,50.0,Color.AQUA));
            VBox vBoxSection = createNewSection(sections.get(sections.size() - 1));
            vBox.getChildren().add(vBox.getChildren().size()-1,vBoxSection);
        });

        buttonRemoveSection.setOnAction(event -> {
            vBox.getChildren().remove(vBox.getChildren().size() - 2);
        });

        scrollPane.setContent(vBox);
        tab.setContent(scrollPane);
        return tab;
    }

    private VBox createNewSection(GaugeSectionPojo gaugeSection) {
        VBox vBox = new VBox();
        vBox.setSpacing(8);
        JFXTextField minTextField = new JFXTextField(String.valueOf(gaugeSection.getStart()));
        minTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            gaugeSection.setStart(Double.parseDouble(newValue));
        });
        JFXTextField maxTextField = new JFXTextField(String.valueOf(gaugeSection.getEnd()));
        maxTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            gaugeSection.setEnd(Double.parseDouble(newValue));
        });
        ColorPickerAdv colorPickerAdv = new ColorPickerAdv();
        colorPickerAdv.setValue(gaugeSection.getColor());
        colorPickerAdv.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            gaugeSection.setColor(newValue);
        });
        HBox hBoxEnd = new HBox(new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.end")), maxTextField);
        hBoxEnd.setPadding(new Insets(5,8,5,8));
        hBoxEnd.setSpacing(8);

        HBox hBoxStart = new HBox(new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.start")), minTextField);
        hBoxStart.setPadding(new Insets(5,8,5,8));
        hBoxStart.setSpacing(8);

        HBox hBoxColor = new HBox(new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.color")), colorPickerAdv);
        hBoxColor.setPadding(new Insets(5,8,5,8));
        hBoxColor.setSpacing(8);

        vBox.getChildren().addAll(hBoxStart,hBoxEnd,hBoxColor,new Separator(Orientation.HORIZONTAL));
        return vBox;
    }

    private VBox createMaxMin() {
        VBox vBox = new VBox();
        vBox.setSpacing(8);

        JFXCheckBox checkBoxInPercent = new JFXCheckBox(I18n.getInstance().getString("plugin.dashboard.gaugewidget.inPercent"));
        checkBoxInPercent.setSelected(inPercent);

        checkBoxInPercent.selectedProperty().addListener((observable, oldValue, newValue) -> {
            inPercent = newValue;
        });


        JFXTextField minTextField = new JFXTextField(String.valueOf(minimum));
        minTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            minimum = Double.parseDouble(newValue);
        });
        JFXTextField maxTextField = new JFXTextField(String.valueOf(maximum));
        maxTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            maximum = Double.parseDouble(newValue);
        });


        HBox hBoxStartAngle = new HBox(checkBoxInPercent);
        hBoxStartAngle.setPadding(new Insets(5,8,5,8));
        hBoxStartAngle.setSpacing(8);


        HBox hBoxMaximum = new HBox(new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.max")), maxTextField);
        hBoxMaximum.setPadding(new Insets(5,8,5,8));
        hBoxMaximum.setSpacing(8);


        HBox hBoxMinimum = new HBox(new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.min")), minTextField);
        hBoxMinimum.setPadding(new Insets(5,8,5,8));
        hBoxMinimum.setSpacing(8);


        vBox.getChildren().addAll(hBoxStartAngle,hBoxMinimum,hBoxMaximum,new Separator(Orientation.HORIZONTAL));
        return vBox;
    }


    public int getLimitSource() {
        return gaugeWidgetID;
    }

    public int getGaugeWidgetID() {
        return gaugeWidgetID;
    }

    public void setGaugeWidgetID(int gaugeWidgetID) {
        this.gaugeWidgetID = gaugeWidgetID;
    }





    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();


        dataNode.put("minimum", minimum);
        dataNode.put("maximum", maximum);
        dataNode.put("inPercent", inPercent);

        ArrayNode arrayNode = dataNode.putArray("sections");
        for (GaugeSectionPojo gaugeSection:sections) {
            ObjectNode dataNode1 = arrayNode.addObject();
            dataNode1.put("end", gaugeSection.getEnd());
            dataNode1.put("start", gaugeSection.getStart());
            dataNode1.put("color", gaugeSection.getColor().toString());

        }
        System.out.println(dataNode);
        return dataNode;
    }

}
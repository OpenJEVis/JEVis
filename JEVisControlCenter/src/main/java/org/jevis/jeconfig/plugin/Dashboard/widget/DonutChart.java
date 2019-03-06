package org.jevis.jeconfig.plugin.Dashboard.widget;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfigProperty;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SimpleDataHandler;

import java.util.ArrayList;
import java.util.List;

public class DonutChart extends Widget {

    Tile tile = new Tile(Tile.SkinType.DONUT_CHART);

    ChartData chartData1 = new ChartData("Strom", 24.0, Tile.GREEN);
    ChartData chartData2 = new ChartData("Wasser", 10.0, Tile.BLUE);
    ChartData chartData3 = new ChartData("Gas", 12.0, Tile.RED);
    ChartData chartData4 = new ChartData("Lüftung", 13.0, Tile.YELLOW_ORANGE);


    private SimpleDataHandler sampleHandler;

    public DonutChart(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);


    }

    @Override
    public void configChanged() {

    }

    @Override
    public void update(WidgetData data, boolean hasNewData) {

    }

    public SampleHandler getSampleHandler() {
        return sampleHandler;
    }

    @Override
    public void setBackgroundColor(Color color) {
        tile.setBackgroundColor(color);
        tile.setBackgroundColor(color);
    }

    @Override
    public void setTitle(String text) {
        tile.setTitle(text);
    }

    @Override
    public void setFontColor(Color color) {
        tile.setForegroundBaseColor(color);
        tile.setTextColor(color);
        tile.setValueColor(color);
    }

    @Override
    public void setCustomFont(Font font) {
        tile.setCustomFont(font);
    }

    @Override
    public void init() {

        tile = TileBuilder.create()
                .skinType(Tile.SkinType.DONUT_CHART)
                .prefSize(config.size.get().getWidth(), config.size.get().getHeight())
                .title("")
                .text("")
                .textVisible(false)
//                .chartData(chartData1, chartData2, chartData3, chartData4)
                .backgroundColor(config.backgroundColor.getValue())
                .build();

        addCommonConfigListeners();

        sampleHandler = new SimpleDataHandler(getDataSource());
        sampleHandler.setMultiSelect(true);
        sampleHandler.lastUpdate.addListener((observable, oldValue, newValue) -> {
            System.out.println("sample Handler indicates update");
            sampleHandler.getValuePropertyMap().forEach((s, samplesList) -> {
                try {
                    System.out.println("Update with samples: " + samplesList.size());
                    if (!samplesList.isEmpty()) {
                        if (samplesList.size() > 1) {
                            String name = sampleHandler.getAttributeMap().get(s).getObject().getName();
                            ChartData chartData = new ChartData(name, samplesList.get(samplesList.size() - 1).getValueAsDouble(), Tile.GREEN);
                            tile.setValue(samplesList.get(samplesList.size() - 1).getValueAsDouble());
                            tile.setReferenceValue(samplesList.get(samplesList.size() - 2).getValueAsDouble());
                            tile.getChartData().add(chartData);
                        }
                        tile.setValue(samplesList.get(samplesList.size() - 1).getValueAsDouble());
                    } else {
                        tile.setValue(0.0);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });


        StringProperty textProperty = new SimpleStringProperty("");
        StringProperty descriptionProperly = new SimpleStringProperty("");
        textProperty.addListener((observable, oldValue, newValue) -> {
            tile.setText(newValue);
        });
        descriptionProperly.addListener((observable, oldValue, newValue) -> {
            tile.setDescription(newValue);
        });
        config.unit.addListener((observable, oldValue, newValue) -> {
            tile.setUnit(newValue);
        });


        String category = "Donut Widget";

        List<WidgetConfigProperty> propertyList = new ArrayList<>();
        propertyList.add(new WidgetConfigProperty<String>("Widget.Text", category, "Text", "", textProperty));
        propertyList.add(new WidgetConfigProperty<String>("Widget.Description", category, "Description", "", descriptionProperly));


        config.addAdditionalSetting(propertyList);


        tile.setAnimated(true);
        setGraphic(tile);
    }


    @Override
    public String typeID() {
        return "Donut";
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutChart.png", previewSize.getHeight(), previewSize.getWidth());
    }
}

package org.jevis.jeconfig.plugin.Dashboard.widget;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;

public class DonutChart extends Widget {

    Tile tile = new Tile(Tile.SkinType.DONUT_CHART);

    ChartData chartData1 = new ChartData("Strom", 24.0, Tile.GREEN);
    ChartData chartData2 = new ChartData("Wasser", 10.0, Tile.BLUE);
    ChartData chartData3 = new ChartData("Gas", 12.0, Tile.RED);
    ChartData chartData4 = new ChartData("Lüftung", 13.0, Tile.YELLOW_ORANGE);

    @Override
    public void update(WidgetData data, boolean hasNewData) {

    }

    @Override
    public void init() {

        tile = TileBuilder.create()
                .skinType(Tile.SkinType.DONUT_CHART)
                .prefSize(config.size.get().getWidth(), config.size.get().getHeight())
                .title("DonutChart")
                .text("Some text")
                .textVisible(false)
                .chartData(chartData1, chartData2, chartData3, chartData4)
                .backgroundColor(config.backgroundColor.getValue())
                .build();

        config.backgroundColor.addListener((observable, oldValue, newValue) -> {
//            tile.setBackgroundColor(newValue);
            tile.setForegroundBaseColor(newValue);
        });

        config.fontColor.addListener((observable, oldValue, newValue) -> {
            tile.setTextColor(newValue);
        });


        tile.setAnimated(true);
        setGraphic(tile);
    }

    @Override
    public String typeID() {
        return "Tile-Donut";
    }
}

package org.jevis.jeconfig.plugin.Dashboard.widget;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.LastValueHandler;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;

public class StockWidget extends Widget {

    Tile tile = new Tile(Tile.SkinType.DONUT_CHART);

    private LastValueHandler sampleHandler;

    public StockWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
        sampleHandler = new LastValueHandler(jeVisDataSource);
        sampleHandler.setMultiSelect(true);
        sampleHandler.lastUpdate.addListener((observable, oldValue, newValue) -> {
            System.out.println("sample Handler indicates update");
            sampleHandler.getValuePropertyMap().forEach((s, samplesList) -> {
                try {
                    System.out.println("Update with samples: " + samplesList.size());
                    if (!samplesList.isEmpty()) {
//                        final AtomicDouble min = new AtomicDouble(Double.MIN_VALUE);
//                        final AtomicDouble max = new AtomicDouble(Double.MAX_VALUE);
//                        samplesList.forEach(jeVisSample -> {
//                            try {
//                                if (min.get() == Double.MIN_VALUE) {
//                                    min.set(jeVisSample.getValueAsDouble());
//                                }else{
//                                    if(min.get()>jeVisSample.getValueAsDouble()){
//                                        min.set(jeVisSample.getValueAsDouble());
//                                    }
//                                }
//
//                                if (max.get() == Double.MAX_VALUE) {
//                                    max.set(jeVisSample.getValueAsDouble());
//                                }else{
//                                    if(max.get()<jeVisSample.getValueAsDouble()){
//                                        max.set(jeVisSample.getValueAsDouble());
//                                    }
//                                }
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
//                        });
//
//                        tile.minValue(min.get());
//                        tile.maxValue(max.get());


                        samplesList.forEach(jeVisSample -> {


                            Platform.runLater(() -> {
                                try {
                                    System.out.println("Sect value: " + jeVisSample.getValueAsDouble());
                                    tile.setValue(jeVisSample.getValueAsDouble());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });


                        });

                    } else {
                        tile.setValue(0.0);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

    }

    @Override
    public void setBackgroundColor(Color color) {
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
    public void update(WidgetData data, boolean hasNewData) {

    }

    public SampleHandler getSampleHandler() {


        return sampleHandler;

    }

    @Override
    public void init() {

        tile = TileBuilder.create()
                .skinType(Tile.SkinType.STOCK)
                .prefSize(config.size.get().getWidth(), config.size.get().getHeight())
                .backgroundColor(config.backgroundColor.getValue())
//                .title("Stock")
                .minValue(0)
                .maxValue(50)
                .averagingPeriod(100)
                .build();

        addCommonConfigListeners();

        tile.setAnimated(true);
        setGraphic(tile);
    }

    @Override
    public String typeID() {
        return "Stock";
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/Stock.png", previewSize.getHeight(), previewSize.getWidth());
    }
}

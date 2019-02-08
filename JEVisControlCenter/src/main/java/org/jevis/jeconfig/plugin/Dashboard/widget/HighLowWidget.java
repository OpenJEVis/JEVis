package org.jevis.jeconfig.plugin.Dashboard.widget;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.scene.image.ImageView;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.LastValueHandler;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;

public class HighLowWidget extends Widget {

    Tile tile;
    private LastValueHandler sampleHandler;


    public HighLowWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
        sampleHandler = new LastValueHandler(jeVisDataSource);
        sampleHandler.setMultiSelect(false);
        sampleHandler.lastUpdate.addListener((observable, oldValue, newValue) -> {
            System.out.println("sample Handler indicates update");
            sampleHandler.getValuePropertyMap().forEach((s, samplesList) -> {
                try {
                    System.out.println("Update with samples: " + samplesList.size());
                    if (!samplesList.isEmpty()) {
                        if (samplesList.size() > 1) {
                            tile.setValue(samplesList.get(samplesList.size() - 2).getValueAsDouble());
                            tile.setReferenceValue(samplesList.get(samplesList.size() - 2).getValueAsDouble());
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

    }

    @Override
    public void update(WidgetData data, boolean hasNewData) {

    }


    public SampleHandler getSampleHandler() {
//        sampleHandler.getUnitProperty().addListener((observable, oldValue, newValue) -> {
//            try {
//                tile.setUnit(newValue);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
        return sampleHandler;

    }

    @Override
    public void init() {

        tile = TileBuilder.create()
                .skinType(Tile.SkinType.HIGH_LOW)
                .prefSize(config.size.get().getWidth(), config.size.get().getHeight())
                .title("Strom")
//                .unit("kWh")
                .description("")
                .text("")
//                .referenceValue(20.7)
//                .value(26.2)
                .backgroundColor(config.backgroundColor.getValue())
                .textColor(config.fontColor.getValue())
                .build();

        config.backgroundColor.addListener((observable, oldValue, newValue) -> {
//            tile.setBackgroundColor(newValue);
            tile.setBackgroundColor(newValue);
        });

        config.fontColor.addListener((observable, oldValue, newValue) -> {
            tile.setTextColor(newValue);
            tile.setValueColor(newValue);
        });

        config.font.addListener((observable, oldValue, newValue) -> {
            tile.setCustomFont(newValue);
            tile.setCustomFontEnabled(true);
        });
//        tile.setValue(28.7);

        tile.setAnimated(true);
        setGraphic(tile);
    }

    @Override
    public String typeID() {
        return "High Low";
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/HighLow.png", previewSize.getHeight(), previewSize.getWidth());
    }

}

package org.jevis.jeconfig.plugin.object.attribute;

import com.ibm.icu.text.NumberFormat;
import io.jenetics.jpx.GPX;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class GPSEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(GPSEditor.class);
    private final BooleanProperty changed = new SimpleBooleanProperty(this, "changed", false);
    private final BooleanProperty readOnly = new SimpleBooleanProperty(this, "readOnly", false);
    private final JEVisAttribute attribute;
    private final TextField latitudeField = new TextField();
    private final TextField longitudeField = new TextField();
    private final TextField elevationField = new TextField();
    private final Button openMapButton = new Button(I18n.getInstance().getString("plugin.object.editor.gps.label.openmap"));
    private final GridPane gridPane = new GridPane();
    private final DoubleValidator dv = DoubleValidator.getInstance();
    private final NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
    private JEVisSample latestSample;
    private boolean initialized = false;
    private boolean loading = false;

    public GPSEditor(JEVisAttribute attribute) {
        this.attribute = attribute;
        this.nf.setMaximumFractionDigits(6);

        try {
            readOnly.set(attribute.getDataSource().getCurrentUser().canWrite(attribute.getObjectID()));
        } catch (JEVisException e) {
            logger.error("Could not get user rights for object", e);
        }

        latitudeField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String parsedValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                if (!loading) {
                    setChanged(true);
                }
            } catch (Exception e) {
                latitudeField.setText(oldValue);
            }
        });

        longitudeField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String parsedValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                if (!loading) {
                    setChanged(true);
                }
            } catch (Exception e) {
                longitudeField.setText(oldValue);
            }
        });

        elevationField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String parsedValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                if (!loading) {
                    setChanged(true);
                }
            } catch (Exception e) {
                elevationField.setText(oldValue);
            }
        });

        Label latitudeLabel = new Label(I18n.getInstance().getString("plugin.object.editor.gps.label.latitude"));
        Label longitudeLabel = new Label(I18n.getInstance().getString("plugin.object.editor.gps.label.longitude"));
        Label elevationLabel = new Label(I18n.getInstance().getString("plugin.object.editor.gps.label.elevation"));

        openMapButton.setOnAction(actionEvent -> openMap(actionEvent, latitudeField.getText(), longitudeField.getText()));

        gridPane.setPadding(new Insets(6));
        gridPane.setHgap(6);
        gridPane.setVgap(6);
        gridPane.addColumn(0, latitudeLabel, longitudeLabel, elevationLabel);
        gridPane.addColumn(1, latitudeField, longitudeField, elevationField);
        gridPane.addColumn(2, new Region(), openMapButton, new Region());
    }

    public static void openMap(ActionEvent actionEvent, String lat, String lon) {
        String os = System.getProperty("os.name").toLowerCase();
        String url = "http://www.openstreetmap.org/?lat=" + lat + "&lon=" + lon + "&zoom=17&layers=M";

        try {
            if (os.contains("win")) {
                Runtime rt = Runtime.getRuntime();
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime rt = Runtime.getRuntime();
                rt.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime rt = Runtime.getRuntime();
                String[] browsers = {"google-chrome", "firefox", "mozilla", "epiphany", "konqueror",
                        "netscape", "opera", "links", "lynx"};

                StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++)
                    if (i == 0)
                        cmd.append(String.format("%s \"%s\"", browsers[i], url));
                    else
                        cmd.append(String.format(" || %s \"%s\"", browsers[i], url));
                // If the first didn't work, try the next browser and so on

                rt.exec(new String[]{"sh", "-c", cmd.toString()});
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void init() {


        if (attribute.getLatestSample() != null) {
            loading = true;
            latestSample = attribute.getLatestSample();

            try {
                JEVisFile jeVisFile = latestSample.getValueAsFile();
                File tmpFile = File.createTempFile(jeVisFile.getFilename(), "tmp");
                jeVisFile.saveToFile(tmpFile);

                InputStream inputStream = Files.newInputStream(tmpFile.toPath());
                GPX.read(inputStream).tracks()
                        .forEach(trackSegments -> {
                            trackSegments.getSegments().forEach(wayPoints -> wayPoints.forEach(wayPoint -> {
                                double lat = wayPoint.getLatitude().doubleValue();
                                double lon = wayPoint.getLongitude().doubleValue();
                                double ele = wayPoint.getElevation().get().doubleValue();
                                loading = true;
                                latitudeField.setText(nf.format(lat));
                                longitudeField.setText(nf.format(lon));
                                elevationField.setText(dv.format(ele));
                                loading = false;
                            }));
                        });


                inputStream.close();

            } catch (Exception e) {
                logger.error("IOException while loading file", e);
            }

            loading = false;
        }

        initialized = true;
    }

    @Override
    public boolean hasChanged() {
        return changed.get();
    }

    @Override
    public void commit() throws JEVisException {


        try {
            JEVisSample fileSample = attribute.buildSample(new DateTime(), getNewFile());
            fileSample.commit();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public JEVisFile getNewFile() throws IOException {
        GPX gpx = GPX.builder()
                .addTrack(track -> track
                        .addSegment(segment -> segment.addPoint(
                                p -> p.lat(dv.validate(latitudeField.getText()))
                                        .lon(dv.validate(longitudeField.getText()))
                                        .ele(dv.validate(elevationField.getText()))
                        ))).build();

        File tmpFile = File.createTempFile("gpx", "tmp");
        GPX.write(gpx, tmpFile.getAbsolutePath());

        return new JEVisFileImp("location.gpx", tmpFile);
    }

    @Override
    public Node getEditor() {
        try {
            if (!initialized) {
                init();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return gridPane;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed.set(changed);
    }

    @Override
    public void setReadOnly(boolean canRead) {
        readOnly.set(canRead);
    }

    @Override
    public JEVisAttribute getAttribute() {
        return attribute;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            gridPane.getChildren().clear();
            init();
        });
    }
}

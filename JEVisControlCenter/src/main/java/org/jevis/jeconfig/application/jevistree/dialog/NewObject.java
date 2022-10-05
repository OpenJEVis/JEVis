package org.jevis.jeconfig.application.jevistree.dialog;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.dialog.*;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class NewObject {

    private static final Logger logger = LogManager.getLogger(NewObject.class);

    public static void NewObject(final JEVisTree tree, JEVisObject parent) {
        try {
            if (parent != null && tree.getJEVisDataSource().getCurrentUser().canCreate(parent.getID())) {
                NewObjectDialog dia = new NewObjectDialog();

                if (dia.show(null, parent, false, NewObjectDialog.Type.NEW, null) == NewObjectDialog.Response.YES) {

                    final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.member.create") + "...");

                    Task<Void> upload = new Task<Void>() {
                        @Override
                        protected Void call() {
                            for (int i = 0; i < dia.getCreateCount(); i++) {
                                try {
                                    String name = dia.getCreateName();
                                    if (dia.getCreateCount() > 1) {
                                        name += " " + (i + 1);
                                    }

                                    JEVisClass createClass = dia.getCreateClass();

                                    if (!dia.getTemplate().isNotAnTemplate()) {

                                        if (dia.getTemplate().create(createClass, parent, name)) {
                                            succeeded();
                                        }

                                    } else {
                                        JEVisObject newObject = parent.buildObject(name, createClass);
                                        newObject.commit();

                                        try {
                                            createDefaultValues(newObject, dia.isWithCleanData());
                                        } catch (Exception ex) {
                                            logger.error("Error while setting default values; {}", ex, ex);
                                        }

                                        succeeded();
                                    }


                                } catch (JEVisException ex) {
                                    logger.catching(ex);

                                    if (ex.getMessage().equals("Can not create User with this name. The User has to be unique on the System")) {
                                        InfoDialog info = new InfoDialog();
                                        info.show("Waring", "Could not create user", "Could not create new user because this user exists already.");

                                    } else {
                                        ExceptionDialog errorDia = new ExceptionDialog();
                                        errorDia.show("Error", ex.getLocalizedMessage(), ex.getLocalizedMessage(), ex, null);

                                    }
                                    failed();
                                }
                            }
                            succeeded();
                            return null;
                        }
                    };
                    upload.setOnSucceeded(event -> pForm.getDialogStage().close());

                    upload.setOnCancelled(event -> {
                        logger.error(I18n.getInstance().getString("plugin.object.waitsave.canceled"));
                        pForm.getDialogStage().hide();
                    });

                    upload.setOnFailed(event -> {
                        logger.error(I18n.getInstance().getString("plugin.object.waitsave.failed"));
                        pForm.getDialogStage().hide();
                    });

                    pForm.activateProgressBar(upload);
                    pForm.getDialogStage().show();

                    new Thread(upload).start();
                }
            } else {
                Platform.runLater(() -> {
                    Alert alert1 = new Alert(Alert.AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                    alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                    alert1.showAndWait();
                });
            }
        } catch (Exception e) {
            logger.error("Could not get jevis data source.", e);
        }
    }

    public static void createDefaultValues(JEVisObject newObject, boolean isCleanData) throws JEVisException {
        JEVisClass dataClass = newObject.getDataSource().getJEVisClass("Data");
        JEVisClass cleanDataClass = newObject.getDataSource().getJEVisClass("Clean Data");
        JEVisClass reportClass = newObject.getDataSource().getJEVisClass("Periodic Report");
        JEVisClass calculationClass = newObject.getDataSource().getJEVisClass("Calculation");
        JEVisClass createClass = newObject.getJEVisClass();

        if (createClass.equals(dataClass) || createClass.equals(cleanDataClass)) {
            JEVisAttribute valueAttribute = newObject.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
            valueAttribute.setInputSampleRate(Period.minutes(15));
            valueAttribute.setDisplaySampleRate(Period.minutes(15));
            valueAttribute.commit();

            if (createClass.equals(dataClass) && isCleanData) {
                JEVisObject newCleanObject = newObject.buildObject(I18nWS.getInstance().getClassName(cleanDataClass), cleanDataClass);
                newCleanObject.commit();

                JEVisAttribute cleanDataValueAttribute = newCleanObject.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
                cleanDataValueAttribute.setInputSampleRate(Period.minutes(15));
                cleanDataValueAttribute.setDisplaySampleRate(Period.minutes(15));
                cleanDataValueAttribute.commit();
            }

        } else if (createClass.equals(reportClass)) {
            Platform.runLater(() -> new ReportWizardDialog(newObject, ReportWizardDialog.NEW));
        } else if (createClass.equals(calculationClass)) {
            JEVisAttribute div0Att = newObject.getAttribute("DIV0 Handling");
            JEVisAttribute staticValueAtt = newObject.getAttribute("Static Value");
            JEVisAttribute allZeroAtt = newObject.getAttribute("All Zero Value");

            DateTime ts = new DateTime(1990, 1, 1, 0, 0, 0);

            staticValueAtt.buildSample(ts, 0).commit();
            allZeroAtt.buildSample(ts, 0).commit();


        }

    }

}

package org.jevis.jeconfig.plugin.action.ui.tab;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.FileData;
import org.jevis.jeconfig.plugin.action.ui.FileTableView;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class AttachmentTab extends Tab {

    public AttachmentTab(ActionData data) {
        super(I18n.getInstance().getString("actionform.editor.tab.attachment"));


        AnchorPane anchorPane = new AnchorPane();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(8));
        gridPane.setHgap(15);
        anchorPane.getChildren().add(gridPane);
        AnchorPane.setTopAnchor(gridPane, 0.0);
        AnchorPane.setLeftAnchor(gridPane, 0.0);
        AnchorPane.setRightAnchor(gridPane, 0.0);
        AnchorPane.setBottomAnchor(gridPane, 0.0);
        setContent(anchorPane);


        //ToggleButton renameFileButton = new ToggleButton("", JEConfig.getSVGImage(Icon.R, iconSize, iconSize));
        ObservableList<FileData> fileData = FXCollections.observableArrayList();
        FileTableView fileTableView = new FileTableView(fileData);
        double iconSize = 12;
        Button addFileButton = new Button("", JEConfig.getSVGImage(Icon.PLUS, iconSize, iconSize));
        Button downloadFileButton = new Button("", JEConfig.getSVGImage(Icon.EXPORT, iconSize, iconSize));
        Button deleteFileButton = new Button("", JEConfig.getSVGImage(Icon.DELETE, iconSize, iconSize));


        addFileButton.setDisable(true);
        try {
            addFileButton.setDisable(!data.getObject().getDataSource().getCurrentUser().canCreate(data.getObject().getID()));
        } catch (Exception ex) {

        }

        deleteFileButton.setOnAction(event -> {
            fileTableView.deleteSelectedFile();
        });

        downloadFileButton.setOnAction(event -> fileTableView.saveSelectedFile());

        addFileButton.setOnAction(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(JEConfig.getStage());
                if (file != null) {
                    JEVisClass fileClass = data.getObject().getDataSource().getJEVisClass(JC.File.name);

                    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    DateTime changeDate = new DateTime(Long.valueOf(attr.lastModifiedTime().toMillis()), DateTimeZone.getDefault());

                    JEVisFileImp jFile = new JEVisFileImp(file.getName(), file);
                    JEVisObject newFileObj = data.getObject().buildObject(file.getName(), fileClass);
                    newFileObj.commit();
                    JEVisAttribute fileAtt = newFileObj.getAttribute(JC.File.a_File);
                    fileAtt.buildSample(new DateTime(), jFile).commit();
                    FileData newFile = new FileData(newFileObj);
                    fileData.add(newFile);

                }

            } catch (Exception xe) {
                xe.printStackTrace();
            }
        });

        try {
            JEVisClass fileClass = data.getObject().getDataSource().getJEVisClass(JC.File.name);
            List<JEVisObject> files = data.getObject().getChildren(fileClass, true);
            files.forEach(jeVisObject -> {
                fileData.add(new FileData(jeVisObject));
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        //fileData.add(new FileData("Checkliste.pdf", new DateTime(), "Florian Simon"));

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().setAll(addFileButton, downloadFileButton, deleteFileButton);
        //ToolBar toolBar = new ToolBar(newButton, addFileButton, removeFileButton);
        // HBox toolbar = new HBox(addFileButton, removeFileButton);
        //toolBar.getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));

        gridPane.add(toolBar, 0, 0, 1, 1);
        gridPane.add(fileTableView, 0, 1, 1, 1);
        GridPane.setHgrow(fileTableView, Priority.ALWAYS);
        GridPane.setVgrow(fileTableView, Priority.ALWAYS);
    }
}

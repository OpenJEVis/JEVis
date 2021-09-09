package org.jevis.jeconfig.plugin.notes;

import com.jfoenix.controls.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.converter.LocalTimeStringConverter;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class NotePane extends StackPane {

    Label targetLabel = new Label(I18n.getInstance().getString("plugin.note.pane.target"));
    Label tagLabel = new Label(I18n.getInstance().getString("plugin.note.pane.tag"));
    Label noteLabel = new Label(I18n.getInstance().getString("plugin.note.pane.note"));
    Label userLabel = new Label(I18n.getInstance().getString("plugin.note.pane.user"));
    Label timeLabel = new Label(I18n.getInstance().getString("plugin.note.pane.time"));


    JFXTextField userField = new JFXTextField();
    TextArea noteTextArea = new TextArea();
    JFXListView<String> tagList = new JFXListView();
    JFXButton targetTreeButton = new JFXButton(I18n.getInstance().getString("plugin.note.pane.opentree"));


    private JFXDatePicker pickerDate = new JFXDatePicker();
    private JFXTimePicker pickerTime = new JFXTimePicker();
    HBox hBox = new HBox(pickerDate, pickerTime);

    JEVisTree jeVisTree;

    private JEVisObject nodeObject;
    private final JEVisDataSource dataSource;
    private JEVisObject parentObject;
    DateTime date = new DateTime();

    public NotePane(ObservableList<String> allTags, JEVisDataSource dataSource, StackPane parentContainer) {

        GridPane gridPane = new GridPane();
        this.dataSource = dataSource;

        jeVisTree = JEVisTreeFactory.buildBasicDefault(this, dataSource, false);

        pickerDate.setPrefWidth(120d);
        pickerTime.setPrefWidth(110d);
        pickerTime.set24HourView(true);
        pickerTime.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));


        LocalDateTime lDate = LocalDateTime.of(
                date.get(DateTimeFieldType.year()), date.get(DateTimeFieldType.monthOfYear()), date.get(DateTimeFieldType.dayOfMonth()), date.get(DateTimeFieldType.hourOfDay()), date.get(DateTimeFieldType.minuteOfHour()), date.get(DateTimeFieldType.secondOfMinute()));
        lDate.atZone(ZoneId.of(date.getZone().getID()));
        pickerDate.valueProperty().setValue(lDate.toLocalDate());
        pickerTime.valueProperty().setValue(lDate.toLocalTime());

        try {
            userField.setText(dataSource.getCurrentUser().getFirstName() + " " + dataSource.getCurrentUser().getLastName() + " <" + dataSource.getCurrentUser().getAccountName() + ">");
            userField.setEditable(false);
            userField.setDisable(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//
        /**
         TreeTableColumn<JEVisTreeRow, JEVisTreeRow> nameCol = ColumnFactory.buildName();
         TreeTableColumn<JEVisTreeRow, Long> idCol = ColumnFactory.buildID();
         BasicCellFilter cellFilter = new BasicCellFilter(I18n.getInstance().getString("tree.filter.nofilter"));
         cellFilter.addItemFilter(new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE));
         jeVisTree.getColumns().addAll(nameCol, idCol);
         jeVisTree.getSortOrder().addAll(nameCol);
         jeVisTree.setSortMode(TreeSortMode.ALL_DESCENDANTS);
         **/

        tagList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        allTags.forEach(s -> {
            tagList.getItems().add(s);
        });

        targetTreeButton.setOnAction(event -> {

            List<UserSelection> openList = new ArrayList<>();
            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataAndCleanDataFilter();
            allFilter.add(allDataFilter);
            //allFilter.add(allAttributesFilter);
            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(
                    parentContainer, allFilter, allDataFilter, null, SelectionMode.SINGLE, dataSource, openList);

            selectTargetDialog.setOnDialogClosed(treeevent -> {
                try {
                    if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                        for (UserSelection us : selectTargetDialog.getUserSelection()) {
                            targetTreeButton.setText(us.getSelectedObject().getName());
                            parentObject = us.getSelectedObject();
                            System.out.println("new Target: " + parentObject.getName());
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            selectTargetDialog.setPrefSize(800, 1200);
            selectTargetDialog.show();
        });

        gridPane.setPadding(new Insets(12));
        gridPane.setVgap(12);
        gridPane.setHgap(12);
        gridPane.addColumn(0, targetLabel, timeLabel, noteLabel, tagLabel, userLabel);
        gridPane.addColumn(1, targetTreeButton, hBox, noteTextArea, tagList, userField);

        getChildren().add(gridPane);


    }

    public DateTime getDate() {
        return date;
    }

    public JEVisObject commit() {
        try {
            JEVisObject newNoteObject;
            JEVisClass noteClass = dataSource.getJEVisClass(NotesPlugin.NOTES_CLASS);
            List<JEVisObject> notes = parentObject.getChildren(noteClass, false);
            if (!notes.isEmpty()) {
                newNoteObject = notes.get(0);
            } else {
                newNoteObject = parentObject.buildObject(noteClass.getName(), noteClass);
                newNoteObject.commit();
            }

            JEVisSample noteSample = newNoteObject.getAttribute("User Notes").buildSample(date, noteTextArea.getText());
            JEVisSample userSample = newNoteObject.getAttribute("User").buildSample(date, userField.getText());

            String tagString = "";
            for (String o : tagList.getSelectionModel().getSelectedItems()) {
                System.out.println("-- selected tag: " + o);
                List<NoteTag> tags = NoteTag.parseTags(o);
                for (NoteTag noteTag : tags) {
                    tagString += noteTag.getId() + ";";
                }
            }
            System.out.println("Tags: " + tagString);
            tagString = tagString.substring(0, tagString.length() - 2);

            JEVisSample tagSample = newNoteObject.getAttribute("Tag").buildSample(date, tagString);

            noteSample.commit();
            userSample.commit();
            tagSample.commit();
            return newNoteObject;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}

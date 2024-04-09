package org.jevis.jeconfig.plugin.notes;

import com.jfoenix.controls.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotePane extends Dialog {

    private static final Logger logger = LogManager.getLogger(NotePane.class);
    Label targetLabel = new Label(I18n.getInstance().getString("plugin.note.pane.target"));
    Label tagLabel = new Label(I18n.getInstance().getString("plugin.note.pane.tag"));
    Label noteLabel = new Label(I18n.getInstance().getString("plugin.note.pane.note"));
    Label userLabel = new Label(I18n.getInstance().getString("plugin.note.pane.user"));
    Label timeLabel = new Label(I18n.getInstance().getString("plugin.note.pane.time"));


    JFXTextField userField = new JFXTextField();
    TextArea noteTextArea = new TextArea();
    private final JFXDatePicker pickerDate = new JFXDatePicker();
    JFXButton targetTreeButton;
    private final JFXTimePicker pickerTime = new JFXTimePicker();
    JFXListView<String> tagList = new JFXListView<>();
    HBox hBox = new HBox(pickerDate, pickerTime);

    JEVisTree jeVisTree;

    private JEVisObject nodeObject;
    private final JEVisDataSource dataSource;
    private JEVisObject parentObject;
    DateTime date = new DateTime();

   private final Optional<NotesRow> notesRow;

    public NotePane(ObservableList<String> allTags, JEVisDataSource dataSource, Optional<NotesRow> notesRow) {
        this.notesRow = notesRow;
        GridPane gridPane = new GridPane();
        this.dataSource = dataSource;
        parentObject = getParentObject(notesRow);
        if (parentObject != null) {
            targetTreeButton = new JFXButton(parentObject.getName());
        }else {
            targetTreeButton = new JFXButton((I18n.getInstance().getString("plugin.note.pane.opentree")));
        }



        jeVisTree = JEVisTreeFactory.buildBasicDefault(dataSource, false);

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
            openList.add(new UserSelection(UserSelection.SelectionType.Attribute, parentObject));
            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataAndCleanDataFilter();
            allFilter.add(allDataFilter);
            //allFilter.add(allAttributesFilter);
            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allDataFilter,null , SelectionMode.SINGLE, dataSource, openList);

            selectTargetDialog.setOnCloseRequest(treeevent -> {
                try {
                    if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                        for (UserSelection us : selectTargetDialog.getUserSelection()) {
                            targetTreeButton.setText(us.getSelectedObject().getName());
                            parentObject = us.getSelectedObject();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            selectTargetDialog.getDialogPane().setPrefSize(800, 1200);
            selectTargetDialog.show();
        });

        gridPane.setPadding(new Insets(12));
        gridPane.setVgap(12);
        gridPane.setHgap(12);
        gridPane.addColumn(0, targetLabel, timeLabel, noteLabel, tagLabel, userLabel);
        gridPane.addColumn(1, targetTreeButton, hBox, noteTextArea, tagList, userField);

        getDialogPane().setContent(gridPane);


    }

    private static JEVisObject getParentObject(Optional<NotesRow> notesRow) {
        try{
            return notesRow.orElseThrow(RuntimeException::new).getObject().getParent();
        }catch (Exception e){
            return null;
        }
    }

    public DateTime getDate() {
        return date;
    }

    public Optional<NotesRow> commit() {
        try {
            JEVisObject newNoteObject;
            JEVisClass noteClass = dataSource.getJEVisClass(NotesPlugin.NOTES_CLASS);
            List<JEVisObject> notes = parentObject.getChildren(noteClass, false);
            if (!notes.isEmpty()) {
                logger.debug("Note Object exists");
                newNoteObject = notes.get(0);
            } else {
                logger.debug("create new Note Object");
                newNoteObject = parentObject.buildObject(noteClass.getName(), noteClass);
                newNoteObject.commit();
            }

            LocalDate ldate = pickerDate.valueProperty().get();
            LocalTime ltime = pickerTime.valueProperty().get();
            date = new DateTime(ldate.getYear(), ldate.getMonthValue(), ldate.getDayOfMonth(), ltime.getHour(), ltime.getMinute());

            JEVisSample noteSample = newNoteObject.getAttribute("Value").buildSample(date, noteTextArea.getText());
            JEVisSample userSample = newNoteObject.getAttribute("User").buildSample(date, userField.getText());

            String tagString = "";
            for (String o : tagList.getSelectionModel().getSelectedItems()) {
                List<NoteTag> tags = NoteTag.parseTags(o);
                for (NoteTag noteTag : tags) {
                    tagString += noteTag.getId() + ";";
                }
            }
            logger.debug("Tags: " + tagString);


            JEVisSample tagSample = newNoteObject.getAttribute("Tag").buildSample(date, tagString);

            noteSample.commit();
            userSample.commit();
            tagSample.commit();
            return getNotesRow(getDate(), noteTextArea.getText(), newNoteObject, tagString, userField.getText());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    private Optional<NotesRow> getNotesRow(DateTime date, String note, JEVisObject jeVisObject, String tag, String user){
        try {
            return Optional.of(new NotesRow(date, note, jeVisObject, tag, user));

        } catch (Exception e) {
            logger.error(e);
            return Optional.empty();
        }
    }


}

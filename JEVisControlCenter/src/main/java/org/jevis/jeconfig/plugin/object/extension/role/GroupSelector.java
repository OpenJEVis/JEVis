package org.jevis.jeconfig.plugin.object.extension.role;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;

import java.util.Comparator;
import java.util.List;

public class GroupSelector extends MFXTextField {

    private static final Logger logger = LogManager.getLogger(GroupSelector.class);

    public GroupSelector(JEVisDataSource ds, Role role) {

        ObservableList<JEVisObject> possibleGroups = FXCollections.observableArrayList();


        try {
            List<JEVisObject> allUsers = ds.getObjects(ds.getJEVisClass("Group"), true);
            allUsers.sort(Comparator.comparing(jeVisObject -> jeVisObject.getName()));
            for (JEVisObject group : allUsers) {
                boolean contains = false;
                for(Membership membership:role.getMemberships()){
                    if(membership.getGroupid().equals(group.getID())){
                        contains=true;
                        break;
                    }
                }
                if (!contains) {
                    possibleGroups.add(group);
                }
            }
        } catch (Exception ex) {
            logger.fatal(ex);
        }

        setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));

        FilteredList<JEVisObject> filteredData = new FilteredList<>(possibleGroups, s -> true);
        textProperty().addListener(obs -> {
            String filter = getText();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredData.setPredicate(s -> {
                        boolean match = false;
                        String string = ( s.getName()).toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredData.setPredicate(s -> (s.getName()).toLowerCase().contains(filter.toLowerCase()));
                }
            }
           // Platform.runLater(() -> users.getSelectionModel().selectFirst());
        });

       // users.setItems(filteredData);

    }
}

package org.jevis.jeconfig.plugin.object.extension;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.extension.role.Membership;
import org.jevis.jeconfig.plugin.object.extension.role.Role;
import org.jevis.jeconfig.plugin.object.extension.role.RoleManager;
import org.jevis.jeconfig.plugin.object.extension.role.User;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class RoleExtention implements ObjectEditorExtension {
    private static final Logger logger = LogManager.getLogger(RoleExtention.class);

    private static final String TITLE = I18n.getInstance().getString("plugin.object.role.title");
    private final JEVisObject _obj;

    private final BorderPane _view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final ObservableList<Membership> filteredMemberships = FXCollections.observableArrayList();
    private final ObservableList<User> filteredUsers = FXCollections.observableArrayList();
    private final BooleanProperty filterMemberOnlyProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty filterUserOnlyProperty = new SimpleBooleanProperty(true);
    private Role role;
    private TableView groupTableView;
    private TableView userTableView;
    private boolean needLoad = true;
    private JFXTextField filterFieldGroup;
    private JFXTextField filterFieldUser;
    private RoleManager roleManager;
    private JFXComboBox<JEVisObject> dashboardList;
    private JFXCheckBox overwriteDashboard;
    private Long orgaID = 0L;

    public RoleExtention(JEVisObject obj) {
        this._obj = obj;
    }

    private void fetchRoleData(JEVisObject obj) {
        try {
            roleManager = new RoleManager(obj);
            role = roleManager.getRole();

            try {

                orgaID = CommonMethods.getFirstParentalObjectOfClass(obj, "Organization").getID();

            } catch (Exception ex) {
                logger.error(ex);
            }

        } catch (Exception ex) {
            logger.error(ex);
        }

    }


    @Override
    public void showHelp(boolean show) {

    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {
            if (obj.getJEVisClassName().equals("User Role")) return true;
        } catch (Exception ex) {
        }

        _view.setStyle("-fx-background-color:orangered;");
        return false;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public void setVisible() {
        Platform.runLater(() -> {
            try {
                if (needLoad) {
                    reload();
                    needLoad = false;
                }

            } catch (Exception ex) {
                logger.fatal(ex);
                ex.printStackTrace();
            }

        });

    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean needSave() {
        if (role != null) {
            return role.hasChanged();
        } else return false;
    }

    @Override
    public boolean save() {
        logger.debug("Save");

        if (needSave()) {
            logger.debug("Need save");
            roleManager.commit();
            logger.debug("Done commit");
        }
        saveRole();
        reload();

        return true;
    }

    private void reload() {
        fetchRoleData(_obj);
        Platform.runLater(() -> build(_obj));

    }

    @Override
    public void dismissChanges() {
        _changed.setValue(false);
        //TODO delete changes
    }

    private void saveRole() {
        DateTime now = new DateTime();
        JEVisObject dashboard = dashboardList.getSelectionModel().getSelectedItem();
        if (dashboard == null) return;


        try {
            JEVisSample sample = _obj.getAttribute("Start Dashboard").buildSample(now, dashboard.getID());
            sample.commit();

        } catch (JEVisException e) {
            e.printStackTrace();
        }


        roleManager.getRole().getUsers().forEach(user -> {
            try {
                boolean hasDashboard = user.getUserObject().getAttribute("Start Dashboard").hasSample();
                if (!hasDashboard || overwriteDashboard.isSelected()) {
                    if (user.memberProperty().getValue()) {
                        JEVisSample newSample = user.getUserObject().getAttribute("Start Dashboard").buildSample(now, dashboard.getID());
                        newSample.commit();
                    }
                }

            } catch (Exception ex) {
                logger.error(ex);
            }
        });


    }

    private String getDashboardPrefix(JEVisObject obj) {
        String prefix = "";
        try {
            prefix = obj.getParents().get(0).getParents().get(0).getName() + " / ";
        } catch (Exception ex) {
            logger.error(ex);
        }

        return prefix;
    }

    private JFXComboBox<JEVisObject> buildDashboardListView() {
        JFXComboBox<JEVisObject> view = new JFXComboBox<>();
        try {
            Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<javafx.scene.control.ListView<JEVisObject>, ListCell<JEVisObject>>() {
                @Override
                public ListCell<JEVisObject> call(javafx.scene.control.ListView<JEVisObject> param) {
                    return new ListCell<JEVisObject>() {
                        @Override
                        protected void updateItem(JEVisObject object, boolean empty) {
                            super.updateItem(object, empty);
                            if (empty || object == null) {
                                setText("");
                            } else {

                                String text = getDashboardPrefix(object) + object.getName();
                                setText(text);
                            }
                        }
                    };
                }
            };
            view.setCellFactory(cellFactory);
            view.setButtonCell(cellFactory.call(null));


            List<JEVisObject> allDashboard = new ArrayList<>(_obj.getDataSource().getObjects(_obj.getDataSource().getJEVisClass("Dashboard Analysis"), true));

            view.setItems(FXCollections.observableArrayList(allDashboard));

            try {
                JEVisSample dashboard = _obj.getAttribute("Start Dashboard").getLatestSample();
                if (dashboard != null) {
                    JEVisObject dashboardObject = _obj.getDataSource().getObject(dashboard.getValueAsLong());
                    if (dashboardObject != null) {
                        view.getSelectionModel().select(dashboardObject);
                    }
                }
            } catch (Exception ex) {
                logger.error(ex);
            }

        } catch (Exception ex) {
            logger.error(ex);
        }


        return view;
    }


    private void build(final JEVisObject obj) {
        logger.error("build: {}", obj);

        Label userTitle = new Label(I18n.getInstance().getString("plugin.object.role.users"));
        Label groupTitle = new Label(I18n.getInstance().getString("plugin.object.role.grouprights"));
        JFXCheckBox showActiveMember = new JFXCheckBox(I18n.getInstance().getString("plugin.object.role.autofilter"));
        JFXCheckBox showActiveUser = new JFXCheckBox(I18n.getInstance().getString("plugin.object.role.autofilter"));
        Label dashboardLabel = new Label(I18n.getInstance().getString("plugin.object.role.dashboard"));
        Label filterGroupTable = new Label(I18n.getInstance().getString("plugin.object.role.filter"));
        Label filterUserTable = new Label(I18n.getInstance().getString("plugin.object.role.filter"));
        filterFieldGroup = new JFXTextField();
        filterFieldUser = new JFXTextField();
        overwriteDashboard = new JFXCheckBox(I18n.getInstance().getString("plugin.object.role.overwritedashboard"));
        filterFieldGroup.setPromptText(I18n.getInstance().getString("plugin.object.role.filterprompt"));
        filterFieldUser.setPromptText(I18n.getInstance().getString("plugin.object.role.filterprompt"));
        HBox userFilterBox = new HBox(8, filterUserTable, filterFieldUser);
        userFilterBox.setAlignment(Pos.CENTER_RIGHT);
        HBox groupFilterBox = new HBox(8, filterGroupTable, filterFieldGroup);
        groupFilterBox.setAlignment(Pos.CENTER_RIGHT);

        Label newUserLabel = new Label(I18n.getInstance().getString("plugin.object.member.addmember"));
        newUserLabel.setPrefHeight(21);


        GridPane gridPane = new GridPane();
        gridPane.setId("rolegrid");
        gridPane.setHgap(4);
        gridPane.setVgap(8);
        gridPane.setMinWidth(_view.getLayoutBounds().getWidth());

        try {
            buildUserTable();
            buildGroupTable();
            userTableView.setMinWidth(_view.getLayoutBounds().getWidth());
            groupTableView.setMinWidth(_view.getLayoutBounds().getWidth());
            dashboardList = buildDashboardListView();

            int row = 0;

            gridPane.addRow(row, dashboardLabel, dashboardList, overwriteDashboard);
            gridPane.add(new Separator(Orientation.HORIZONTAL), 0, ++row, 3, 1);
            gridPane.addRow(++row, groupTitle);
            gridPane.addRow(++row, showActiveMember);
            gridPane.add(groupFilterBox, 2, row, 1, 1);
            gridPane.add(groupTableView, 0, ++row, 3, 1);
            GridPane.setHgrow(groupTableView, Priority.ALWAYS);
            GridPane.setFillWidth(groupTableView, true);
            gridPane.add(new Separator(Orientation.HORIZONTAL), 0, ++row, 3, 1);
            gridPane.addRow(++row, userTitle);
            gridPane.addRow(++row, showActiveUser);
            gridPane.add(userFilterBox, 2, row, 1, 1);
            gridPane.add(userTableView, 0, ++row, 3, 1);
            GridPane.setHgrow(userTableView, Priority.ALWAYS);
            GridPane.setFillWidth(userTableView, true);

            //GridPane.setFillWidth(groupFilterBox, true);
            GridPane.setHalignment(groupFilterBox, HPos.RIGHT);
            GridPane.setHalignment(userFilterBox, HPos.RIGHT);

            filterFieldGroup.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                                    String oldValue, String newValue) {
                    updateFilteredData();
                }
            });
            filterFieldUser.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    updateUserFilteredData();
                }
            });

            showActiveMember.selectedProperty().bindBidirectional(filterMemberOnlyProperty);
            showActiveUser.selectedProperty().bindBidirectional(filterUserOnlyProperty);
            filterMemberOnlyProperty.addListener(observable -> {
                updateFilteredData();
            });
            showActiveUser.selectedProperty().bindBidirectional(filterUserOnlyProperty);
            filterUserOnlyProperty.addListener(observable -> {
                updateUserFilteredData();
            });

        } catch (Exception e) {
            logger.error(e);
        }


        _view.setCenter(gridPane);
    }


    private void buildGroupTable() {
        // user by relationship
        logger.error("buildGroupPane:");

        groupTableView = new TableView<>();
        //groupTableView.setMinWidth(600);

        groupTableView.setEditable(true);
        TableColumn<TableUser, String> nameCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.group"));
        TableColumn<TableUser, String> idCol = new TableColumn<>("ID");
        TableColumn<Membership, Boolean> readCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.read"));
        TableColumn<Membership, Boolean> writeCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.write"));
        TableColumn<Membership, Boolean> deleteCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.delete"));
        TableColumn<Membership, Boolean> createCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.create"));
        TableColumn<Membership, Boolean> executeCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.execute"));

        groupTableView.getColumns().addAll(nameCol, idCol, readCol, writeCol, deleteCol, createCol, executeCol);
        userTableView.getSortOrder().add(nameCol);

        nameCol.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("groupid"));
        readCol.setCellValueFactory(param -> param.getValue().readProperty());
        readCol.setCellFactory(param -> new CheckBoxTableCell<>());
        writeCol.setCellValueFactory(param -> param.getValue().writeProperty());
        writeCol.setCellFactory(param -> new CheckBoxTableCell<>());
        deleteCol.setCellValueFactory(param -> param.getValue().deleteProperty());
        deleteCol.setCellFactory(param -> new CheckBoxTableCell<>());
        createCol.setCellValueFactory(param -> param.getValue().createProperty());
        createCol.setCellFactory(param -> new CheckBoxTableCell<>());
        executeCol.setCellValueFactory(param -> param.getValue().executeProperty());
        executeCol.setCellFactory(param -> new CheckBoxTableCell<>());

        filteredMemberships.addAll(role.getMemberShips());

        groupTableView.setItems(filteredMemberships);


        role.getMemberShips().addListener(new ListChangeListener<Membership>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Membership> change) {
                updateFilteredData();
            }
        });
        updateFilteredData();
    }


    private void reapplyTableSortOrderUser() {
        ArrayList<TableColumn<User, ?>> sortOrder = new ArrayList<>(userTableView.getSortOrder());
        userTableView.getSortOrder().clear();
        userTableView.getSortOrder().addAll(sortOrder);
    }

    private void reapplyTableSortOrder() {
        ArrayList<TableColumn<Membership, ?>> sortOrder = new ArrayList<>(groupTableView.getSortOrder());
        groupTableView.getSortOrder().clear();
        groupTableView.getSortOrder().addAll(sortOrder);
    }

    private boolean matchesFilter(Membership membership) {
        String filterString = filterFieldGroup.getText();
        if (filterString == null || filterString.isEmpty()) {
            // No filter --> Add all.
            return true;
        }
        String lowerCaseFilterString = filterString.toLowerCase();

        if (membership.getGroupName().toLowerCase().contains(lowerCaseFilterString)) {
            return true;
        } else return membership.getGroupName().toLowerCase().contains(lowerCaseFilterString);// Does not match
    }


    private void buildUserTable() throws JEVisException {
        // user by relationship
        logger.error("buildUserTable:");


        userTableView = new TableView<>();
        userTableView.setEditable(true);

        TableColumn<User, Boolean> memberCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.member"));
        TableColumn<User, String> userNameCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.User"));
        TableColumn<User, Number> idCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.role.table.ID"));
        userTableView.getColumns().addAll(memberCol, userNameCol, idCol);

        memberCol.setCellValueFactory(param -> param.getValue().memberProperty());
        memberCol.setCellFactory(param -> new CheckBoxTableCell<>());
        memberCol.setEditable(true);
        userNameCol.setCellValueFactory(param -> param.getValue().usernameProperty());
        userNameCol.setEditable(false);
        idCol.setCellValueFactory(param -> param.getValue().idProperty());

        filteredUsers.addAll(role.getUsers());
        userTableView.setItems(filteredUsers);
        userTableView.getSortOrder().add(userNameCol);

        role.getUsers().addListener(new ListChangeListener<User>() {
            @Override
            public void onChanged(Change<? extends User> c) {
                updateUserFilteredData();
            }
        });

        updateUserFilteredData();
    }

    private void updateFilteredData() {
        filteredMemberships.clear();

        for (Membership p : role.getMemberShips()) {
            boolean isFilerMatch = matchesFilter(p);
            if (filterMemberOnlyProperty.getValue()) {
                if (isFilerMatch && (p.isRead() || p.isCreate() || p.isDelete() || p.isExecute() || p.isWrite() || isInSameOrga(p.getGroupObject()))) {
                    filteredMemberships.add(p);
                }
            } else if (isFilerMatch) {
                filteredMemberships.add(p);
            }

        }

        // Must re-sort table after items changed
        reapplyTableSortOrder();
    }

    private boolean isInSameOrga(JEVisObject obj) {
        try {
            JEVisObject buildingParent = CommonMethods.getFirstParentalObjectOfClass(obj, "Building");
            JEVisObject organisationParent = CommonMethods.getFirstParentalObjectOfClass(obj, "Organisation");
            if (orgaID.equals(buildingParent.getID()) || orgaID.equals(organisationParent.getID())) return true;
        } catch (Exception ex) {
            logger.error(ex);
        }
        return false;
    }

    private void updateUserFilteredData() {
        filteredUsers.clear();

        for (User u : role.getUsers()) {
            boolean isFilerMatch = matchesFilter(u);
            if (filterUserOnlyProperty.getValue()) {
                if (isFilerMatch && (u.getMember() || isInSameOrga(u.getUserObject()))) {
                    filteredUsers.add(u);
                }
            } else if (isFilerMatch) {
                filteredUsers.add(u);
            }

        }

        // Must re-sort table after items changed
        reapplyTableSortOrderUser();
    }


    private boolean matchesFilter(User user) {
        String filterString = filterFieldUser.getText();
        if (filterString == null || filterString.isEmpty()) {
            // No filter --> Add all.
            return true;
        }
        String lowerCaseFilterString = filterString.toLowerCase();

        if (user.getUsername().toLowerCase().contains(lowerCaseFilterString)) {
            return true;
        } else return user.getUsername().toLowerCase().contains(lowerCaseFilterString);// Does not match
    }


    public static class TableUser {

        private final SimpleStringProperty user;
        private final SimpleLongProperty id;

        private TableUser(String user, Long id) {
            this.user = new SimpleStringProperty(user);
            this.id = new SimpleLongProperty(id);
        }

        public String getUser() {
            return user.get();
        }

        public void setUser(String user) {
            this.user.set(user);
        }

        public SimpleStringProperty userProperty() {
            return user;
        }

        public long getId() {
            return id.get();
        }

        public void setId(long id) {
            this.id.set(id);
        }

        public SimpleLongProperty idProperty() {
            return id;
        }
    }


}

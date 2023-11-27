package org.jevis.jecc.plugin.object.extension;


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
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.object.ObjectEditorExtension;
import org.jevis.jecc.plugin.object.extension.role.Membership;
import org.jevis.jecc.plugin.object.extension.role.Role;
import org.jevis.jecc.plugin.object.extension.role.RoleManager;
import org.jevis.jecc.plugin.object.extension.role.User;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class RoleExtention implements ObjectEditorExtension {
    private static final Logger logger = LogManager.getLogger(RoleExtention.class);

    private static final String TITLE = I18n.getInstance().getString("plugin.object.role.title");
    private final JEVisObject _obj;

    private final BorderPane _view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final ObservableList<Membership> filterdMemberships = FXCollections.observableArrayList();
    private final ObservableList<User> filterdUsers = FXCollections.observableArrayList();
    private final BooleanProperty filterMemberOnlyProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty filterUserOnlyProperty = new SimpleBooleanProperty(true);
    private Role role;
    private TableView groupTableView;
    private TableView userTableView;
    private boolean needLoad = true;
    private TextField filterFieldGroup;
    private TextField filterFieldUser;
    private RoleManager roleManager;
    private ComboBox<JEVisObject> dashboadList;
    private CheckBox overwriteDashboad;
    private Long orgaID = 0L;

    public RoleExtention(JEVisObject obj) {
        this._obj = obj;
    }

    private void fetchRoleData(JEVisObject obj) {
        try {
            roleManager = new RoleManager(obj);
            role = roleManager.getRole();

            try {
                orgaID = obj.getParents().get(0).getParents().get(0).getID();

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
        saveDashboard();
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

    private void saveDashboard() {
        DateTime now = new DateTime();
        JEVisObject dashboard = dashboadList.getSelectionModel().getSelectedItem();
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
                if (!hasDashboard || overwriteDashboad.isSelected()) {
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

    private ComboBox<JEVisObject> buildDashboardListView() {
        ComboBox<JEVisObject> view = new ComboBox<>();
        List<JEVisObject> allDashboard = new ArrayList<>();
        try {

            //TODO JFX17
            view.setConverter(new StringConverter<JEVisObject>() {
                @Override
                public String toString(JEVisObject object) {
                    return getDashboardPrefix(object) + object.getName();
                }

                @Override
                public JEVisObject fromString(String string) {
                    return view.getItems().get(view.getSelectionModel().getSelectedIndex());
                }
            });


            allDashboard.addAll(_obj.getDataSource().getObjects(_obj.getDataSource().getJEVisClass("Dashboard Analysis"), true));

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


        ScrollPane scroll = new ScrollPane();
        scroll.setMaxSize(10000, 10000);

        Label userTitle = new Label(I18n.getInstance().getString("plugin.object.role.users"));
        Label groupTitle = new Label(I18n.getInstance().getString("plugin.object.role.grouprights"));
        CheckBox showActiveMember = new CheckBox(I18n.getInstance().getString("plugin.object.role.autofilter"));
        CheckBox showActiveUser = new CheckBox(I18n.getInstance().getString("plugin.object.role.autofilter"));
        Label dashboardLabel = new Label(I18n.getInstance().getString("plugin.object.role.dashboard"));
        Label filterGroupTable = new Label(I18n.getInstance().getString("plugin.object.role.filter"));
        Label filterUserTable = new Label(I18n.getInstance().getString("plugin.object.role.filter"));
        filterFieldGroup = new TextField();
        filterFieldUser = new TextField();
        overwriteDashboad = new CheckBox(I18n.getInstance().getString("plugin.object.role.overwritedashboard"));
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
        gridPane.setMinWidth(600);


        try {
            buildUserTable();
            buildGroupTable();
            userTableView.setMinWidth(700);
            groupTableView.setMinWidth(700);
            dashboadList = buildDashboardListView();

            int raw = 0;

            gridPane.addRow(raw, dashboardLabel, dashboadList, overwriteDashboad);
            gridPane.add(new Separator(Orientation.HORIZONTAL), 0, ++raw, 3, 1);
            gridPane.addRow(++raw, groupTitle);
            gridPane.addRow(++raw, showActiveMember);
            gridPane.add(groupFilterBox, 2, raw, 1, 1);
            gridPane.add(groupTableView, 0, ++raw, 3, 1);
            gridPane.add(new Separator(Orientation.HORIZONTAL), 0, ++raw, 3, 1);
            gridPane.addRow(++raw, userTitle);
            gridPane.addRow(++raw, showActiveUser);
            gridPane.add(userFilterBox, 2, raw, 1, 1);
            gridPane.add(userTableView, 0, ++raw, 3, 1);

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

        groupTableView = new TableView();
        //groupTableView.setMinWidth(600);

        groupTableView.setEditable(true);
        TableColumn nameCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.group"));
        TableColumn idCol = new TableColumn("ID");
        TableColumn<Membership, Boolean> readCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.read"));
        TableColumn<Membership, Boolean> writeCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.write"));
        TableColumn<Membership, Boolean> deleteCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.delete"));
        TableColumn<Membership, Boolean> createCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.create"));
        TableColumn<Membership, Boolean> executeCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.execute"));

        groupTableView.getColumns().addAll(nameCol, idCol, readCol, writeCol, deleteCol, createCol, executeCol);
        userTableView.getSortOrder().add(nameCol);

        nameCol.setCellValueFactory(new PropertyValueFactory<TableUser, String>("groupName"));
        idCol.setCellValueFactory(new PropertyValueFactory<TableUser, String>("groupid"));
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

        filterdMemberships.addAll(role.getMemberships());

        groupTableView.setItems(filterdMemberships);


        role.getMemberships().addListener(new ListChangeListener<Membership>() {
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

        if (membership.getGroupName().toLowerCase().indexOf(lowerCaseFilterString) != -1) {
            return true;
        } else return membership.getGroupName().toLowerCase().indexOf(lowerCaseFilterString) != -1;// Does not match
    }


    private void buildUserTable() throws JEVisException {
        // user by relationship
        logger.error("buildUserTable:");


        userTableView = new TableView();
        userTableView.setEditable(true);

        TableColumn<User, Boolean> memberCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.member"));
        TableColumn<User, String> userNameCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.User"));
        TableColumn<User, Number> idCol = new TableColumn(I18n.getInstance().getString("plugin.object.role.table.ID"));
        userTableView.getColumns().addAll(memberCol, userNameCol, idCol);

        memberCol.setCellValueFactory(param -> param.getValue().memberProperty());
        memberCol.setCellFactory(param -> new CheckBoxTableCell<>());
        memberCol.setEditable(true);
        userNameCol.setCellValueFactory(param -> param.getValue().usernameProperty());
        userNameCol.setEditable(false);
        idCol.setCellValueFactory(param -> param.getValue().idProperty());

        filterdUsers.addAll(role.getUsers());
        userTableView.setItems(filterdUsers);
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
        filterdMemberships.clear();

        for (Membership p : role.getMemberships()) {
            boolean isFilerMatch = matchesFilter(p);
            if (filterMemberOnlyProperty.getValue()) {
                if (isFilerMatch && (p.isRead() || p.isCreate() || p.isDelete() || p.isExecute() || p.isWrite() || isInSameOrga(p.getGroupObject()))) {
                    filterdMemberships.add(p);
                }
            } else if (isFilerMatch) {
                filterdMemberships.add(p);
            }

        }

        // Must re-sort table after items changed
        reapplyTableSortOrder();
    }

    private boolean isInSameOrga(JEVisObject obj) {
        try {
            if (orgaID.equals(obj.getParents().get(0).getParents().get(0).getID())) return true;

        } catch (Exception ex) {
            logger.error(ex);
        }
        return false;
    }

    private void updateUserFilteredData() {
        filterdUsers.clear();

        for (User u : role.getUsers()) {
            boolean isFilerMatch = matchesFilter(u);
            if (filterUserOnlyProperty.getValue()) {
                if (isFilerMatch && (u.getMember() || isInSameOrga(u.getUserObject()))) {
                    filterdUsers.add(u);
                }
            } else if (isFilerMatch) {
                filterdUsers.add(u);
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

        if (user.getUsername().toLowerCase().indexOf(lowerCaseFilterString) != -1) {
            return true;
        } else return user.getUsername().toLowerCase().indexOf(lowerCaseFilterString) != -1;// Does not match
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

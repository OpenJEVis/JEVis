package org.jevis.jeconfig.plugin.legal.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.table.SummeryTable;
import org.jevis.jeconfig.plugin.legal.LegalCadastreController;
import org.jevis.jeconfig.plugin.legal.data.IndexOfLegalProvisions;

public class LegalCadastreTab extends Tab {

    private static final Logger logger = LogManager.getLogger(LegalCadastreTab.class);

    private IndexOfLegalProvisions indexOfLegalProvisions;
    private IndexOfLegalProvisionsTable indexOfLegalProvisionsTable;

    public LegalCadastreTab(IndexOfLegalProvisions indexOfLegalProvisions, LegalCadastreController controller, BooleanProperty update) {
        super();
        this.indexOfLegalProvisions = indexOfLegalProvisions;

        if (indexOfLegalProvisions == null) return;
        textProperty().bind(this.indexOfLegalProvisions.getName());
        this.indexOfLegalProvisions = indexOfLegalProvisions;
        this.indexOfLegalProvisionsTable = new IndexOfLegalProvisionsTable(this.indexOfLegalProvisions, this.indexOfLegalProvisions.getLegislationDataList(), update);


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        double maxListHeight = 100;


        Label lSuche = new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.search"));
        JFXTextField fsearch = new JFXTextField();
        fsearch.setPromptText(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.searchfor"));


        org.jevis.jeconfig.plugin.nonconformities.ui.TagButton categoryButton = new org.jevis.jeconfig.plugin.nonconformities.ui.TagButton(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.category"), indexOfLegalProvisions.getCategories(), indexOfLegalProvisions.getCategories());
        org.jevis.jeconfig.plugin.nonconformities.ui.TagButton scopeButton = new org.jevis.jeconfig.plugin.nonconformities.ui.TagButton(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.scope"), indexOfLegalProvisions.getScopes(), indexOfLegalProvisions.getScopes());
        org.jevis.jeconfig.plugin.nonconformities.ui.TagButton relevanceButton = new org.jevis.jeconfig.plugin.nonconformities.ui.TagButton(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.relevance"), indexOfLegalProvisions.getRelevanzTags(), indexOfLegalProvisions.getRelevanzTags());


        indexOfLegalProvisionsTable.setCategories(categoryButton.getSelectedTags());
        indexOfLegalProvisionsTable.setScope(scopeButton.getSelectedTags());
        indexOfLegalProvisionsTable.setRelevance(relevanceButton.getSelectedTags());

        fsearch.textProperty().addListener((observable, oldValue, newValue) -> {
            indexOfLegalProvisionsTable.setTextFilter(newValue);
            indexOfLegalProvisionsTable.filter();
        });

        TimeFilterSelector dateSelector = new TimeFilterSelector(this.indexOfLegalProvisions);


        dateSelector.getValuePropertyProperty().addListener(new ChangeListener<DateFilter>() {
            @Override
            public void changed(ObservableValue<? extends DateFilter> observableValue, DateFilter dateFilter, DateFilter t1) {
                indexOfLegalProvisionsTable.setDateFilter(t1);
                indexOfLegalProvisionsTable.filter();
            }
        });

        categoryButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}", c);
                while (c.next()) {
                    indexOfLegalProvisionsTable.setCategories((ObservableList<String>) c.getList());
                    indexOfLegalProvisionsTable.filter();
                }
            }
        });

        scopeButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}", c);
                while (c.next()) {
                    indexOfLegalProvisionsTable.setScope((ObservableList<String>) c.getList());
                    indexOfLegalProvisionsTable.filter();
                }
            }
        });

        relevanceButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}", c);
                while (c.next()) {
                    indexOfLegalProvisionsTable.setRelevance((ObservableList<String>) c.getList());
                    indexOfLegalProvisionsTable.filter();
                }
            }
        });


        Separator vSep1 = new Separator(Orientation.VERTICAL);
        Separator vSep2 = new Separator(Orientation.VERTICAL);


        GridPane.setRowSpan(vSep1, 2);
        GridPane.setRowSpan(vSep2, 2);
        gridPane.addColumn(0, lSuche, fsearch);
        gridPane.addColumn(1, vSep1);
        //gridPane.addColumn(2, new Label("Relevant"), relevantFilter);
        gridPane.addColumn(3, new Region(), relevanceButton);
        gridPane.addColumn(4, new Region(), categoryButton);
        gridPane.addColumn(5, new Region(), scopeButton);
        gridPane.addColumn(6, vSep2);
        gridPane.addColumn(7, new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.date")), dateSelector);


        indexOfLegalProvisionsTable.filter();


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(gridPane);
        borderPane.setCenter(indexOfLegalProvisionsTable);


        SummeryTable summeryTable = new SummeryTable(indexOfLegalProvisionsTable);
        summeryTable.setItems(indexOfLegalProvisionsTable.getSummeryData());

        borderPane.setBottom(summeryTable);


        indexOfLegalProvisionsTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    controller.openDataForm(false);//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });
        setContent(borderPane);


    }

    public LegalCadastreTab(String text, Node content, IndexOfLegalProvisions indexOfLegalProvisions) {
        super(text, content);
        this.indexOfLegalProvisions = indexOfLegalProvisions;
    }

    public IndexOfLegalProvisions getLegalCadastre() {
        return indexOfLegalProvisions;
    }

    public void setLegalCadastre(IndexOfLegalProvisions indexOfLegalProvisions) {
        this.indexOfLegalProvisions = indexOfLegalProvisions;
    }

    public IndexOfLegalProvisionsTable getLegalCadastreTable() {
        return indexOfLegalProvisionsTable;
    }

    public void setLegalCadastreTable(IndexOfLegalProvisionsTable indexOfLegalProvisionsTable) {
        this.indexOfLegalProvisionsTable = indexOfLegalProvisionsTable;
    }
}

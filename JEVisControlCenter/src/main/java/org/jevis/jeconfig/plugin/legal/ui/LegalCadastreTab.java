package org.jevis.jeconfig.plugin.legal.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
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
import org.jevis.jeconfig.plugin.legal.LegalCadastreController;
import org.jevis.jeconfig.plugin.legal.data.LegalCadastre;

public class LegalCadastreTab extends Tab {

    private static final Logger logger = LogManager.getLogger(LegalCadastreTab.class);

    private LegalCadastre legalCadastre;
    private LegalCadastreTable legalCadastreTable;

    public LegalCadastreTab(LegalCadastre legalCadastre, LegalCadastreController controller) {
        super();
        this.legalCadastre = legalCadastre;

        if(legalCadastre == null) return;
        textProperty().bind(this.legalCadastre.getName());
        this.legalCadastre = legalCadastre;
        this.legalCadastreTable = new LegalCadastreTable(this.legalCadastre, this.legalCadastre.getLegislationDataList());


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        double maxListHeight = 100;


        Label lSuche = new Label("Suche");
        JFXTextField fsearch = new JFXTextField();
        fsearch.setPromptText("Suche nach...");


        org.jevis.jeconfig.plugin.nonconformities.ui.TagButton categoryButton = new org.jevis.jeconfig.plugin.nonconformities.ui.TagButton(I18n.getInstance().getString("plugin.Legalcadastre.legislation.category"), legalCadastre.getCategories(), legalCadastre.getCategories());
        org.jevis.jeconfig.plugin.nonconformities.ui.TagButton scopeButton = new org.jevis.jeconfig.plugin.nonconformities.ui.TagButton(I18n.getInstance().getString("plugin.Legalcadastre.legislation.scope"), legalCadastre.getScopes(),  legalCadastre.getScopes());
        org.jevis.jeconfig.plugin.nonconformities.ui.TagButton relevanceButton = new org.jevis.jeconfig.plugin.nonconformities.ui.TagButton(I18n.getInstance().getString("plugin.Legalcadastre.legislation.relvance"), legalCadastre.getRelevanzTags(),  legalCadastre.getRelevanzTags());


        legalCadastreTable.setCategories(categoryButton.getSelectedTags());
        legalCadastreTable.setScope(scopeButton.getSelectedTags());
        legalCadastreTable.setRelevance(relevanceButton.getSelectedTags());


        ComboBox<String> datumBox = new ComboBox<>();
        datumBox.setItems(FXCollections.observableArrayList("Umsetzung", "Abgeschlossen", "Erstellt"));
        datumBox.getSelectionModel().selectFirst();
        JFXTextField filterDatumText = new JFXTextField();
        filterDatumText.setPromptText("Datum...");
        ComboBox<String> comparatorBox = new ComboBox<>();
        comparatorBox.setItems(FXCollections.observableArrayList(">", "<", "="));
        comparatorBox.getSelectionModel().selectFirst();

        fsearch.textProperty().addListener((observable, oldValue, newValue) -> {
            legalCadastreTable.setTextFilter(newValue);
            legalCadastreTable.filter();
        });

        TimeFilterSelector dateSelector = new TimeFilterSelector(this.legalCadastre);




        dateSelector.getValuePropertyProperty().addListener(new ChangeListener<DateFilter>() {
            @Override
            public void changed(ObservableValue<? extends DateFilter> observableValue, DateFilter dateFilter, DateFilter t1) {
                legalCadastreTable.setDateFilter(t1);
                legalCadastreTable.filter();
            }
        });

        categoryButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}",c);
                while (c.next()) {
                    legalCadastreTable.setCategories((ObservableList<String>) c.getList());
                    legalCadastreTable.filter();
                }
            }
        });

        scopeButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}",c);
                while (c.next()) {
                    legalCadastreTable.setScope((ObservableList<String>) c.getList());
                    legalCadastreTable.filter();
                }
            }
        });

        relevanceButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}",c);
                while (c.next()) {
                    System.out.println(c.getList());
                    legalCadastreTable.setRelevance((ObservableList<String>) c.getList());
                    legalCadastreTable.filter();
                }
            }
        });



        Separator vSep1 = new Separator(Orientation.VERTICAL);
        Separator vSep2 = new Separator(Orientation.VERTICAL);
        ;

        GridPane.setRowSpan(vSep1, 2);
        GridPane.setRowSpan(vSep2, 2);
        gridPane.addColumn(0, lSuche, fsearch);
        gridPane.addColumn(1, vSep1);
        //gridPane.addColumn(2, new Label("Relevant"), relevantFilter);
        gridPane.addColumn(3, new Region(), relevanceButton);
        gridPane.addColumn(4, new Region(), categoryButton);
        gridPane.addColumn(5, new Region(), scopeButton);
        gridPane.addColumn(6, vSep2);
        gridPane.addColumn(7, new Label("Zeitbereich"), dateSelector);


        legalCadastreTable.filter();





        BorderPane borderPane = new BorderPane();
        borderPane.setTop(gridPane);
        borderPane.setCenter(legalCadastreTable);


        legalCadastreTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    controller.openDataForm(false);//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });
        setContent(borderPane);


    }

    public LegalCadastreTab(String text, Node content, LegalCadastre legalCadastre) {
        super(text, content);
        this.legalCadastre = legalCadastre;
    }

    public LegalCadastre getLegalCadastre() {
        return legalCadastre;
    }

    public void setLegalCadastre(LegalCadastre legalCadastre) {
        this.legalCadastre = legalCadastre;
    }

    public LegalCadastreTable getLegalCadastreTable() {
        return legalCadastreTable;
    }

    public void setLegalCadastreTable(LegalCadastreTable legalCadastreTable) {
        this.legalCadastreTable = legalCadastreTable;
    }
}

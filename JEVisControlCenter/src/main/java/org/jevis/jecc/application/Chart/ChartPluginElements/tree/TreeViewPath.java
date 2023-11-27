package org.jevis.jecc.application.Chart.ChartPluginElements.tree;

import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.utils.JFXNodeUtils;
import javafx.css.*;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeViewPath extends ScrollPane {

    private static final String DEFAULT_STYLE_CLASS = "jfx-tree-view-path";
    private final PseudoClass firstClass = PseudoClass.getPseudoClass("first");
    private final PseudoClass nextClass = PseudoClass.getPseudoClass("next");
    private final PseudoClass lastClass = PseudoClass.getPseudoClass("last");
    private final Region clip = new Region();
    private final HBox container = new HBox();
    private final StyleableDoubleProperty offset = new SimpleStyleableDoubleProperty(
            TreeViewPath.StyleableProperties.OFFSET,
            TreeViewPath.this,
            "offset",
            10.0);
    private double lastX;

    public TreeViewPath(TreeView<JEVisTreeViewItem> treeView) {

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setClip(clip);
        clip.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3), Insets.EMPTY)));
        backgroundProperty().addListener(observable -> JFXNodeUtils.updateBackground(getBackground(), clip));

        container.getStyleClass().add("buttons-container");
        container.getChildren().add(new Label("Selection Path..."));
        container.setAlignment(Pos.CENTER_LEFT);
        container.widthProperty().addListener(observable -> setHvalue(getHmax()));
        setContent(container);
        setPannable(true);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setFitToHeight(true);
        treeView.getSelectionModel().selectedItemProperty().addListener(observable -> {
            TreeItem<JEVisTreeViewItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
            TreeItem<JEVisTreeViewItem> temp = selectedItem;
            int level = treeView.getTreeItemLevel(temp) - (treeView.isShowRoot() ? 0 : 1);
            if (temp != null) {
                List<Node> newPath = new ArrayList<>();
                while (temp != null) {
                    TreeItem<JEVisTreeViewItem> parent = treeView.isShowRoot() ? temp : temp.getParent();
                    if (parent != null) {
                        Button button = null;
                        if (temp.isLeaf()) {
                            button = createLastButton(temp, parent.getParent());
                            button.pseudoClassStateChanged(lastClass, true);
                        } else if (parent.getParent() == null) {
                            button = createFirstButton(temp);
                            button.pseudoClassStateChanged(firstClass, true);
                        } else {
                            button = createNextButton(temp);
                            button.pseudoClassStateChanged(nextClass, true);
                        }
                        final TreeItem<JEVisTreeViewItem> node = temp;
                        button.setOnAction(action -> treeView.scrollTo(treeView.getRow(node)));
                        final StackPane container = new StackPane(button);
                        container.setPickOnBounds(false);

                        if (parent.getParent() != null) {
                            container.setTranslateX((-getOffset() - 1) * level--);
                        }
                        if (temp != selectedItem) {
                            final SVGGlyph arrow = new SVGGlyph("M366 698l196-196-196-196 60-60 256 256-256 256z", Color.BLACK);
                            arrow.setSizeForWidth(6);
                            arrow.setMouseTransparent(true);
                            StackPane.setAlignment(arrow, Pos.CENTER_RIGHT);
                            container.getChildren().add(arrow);
                        }
                        newPath.add(0, container);
                    }
                    temp = temp.getParent();
                }
                container.getChildren().setAll(newPath);
            }
        });

        container.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            lastX = event.getX();
        });
        container.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            double dx = lastX - event.getX();
            if (Math.abs(dx) > 0.5) {
                double newHVal = (getHvalue() + dx / (container.getWidth()));
                setHvalue(newHVal);
            }
        });

        JFXScrollPane.smoothHScrolling(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return TreeViewPath.StyleableProperties.CHILD_STYLEABLES;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        clip.resizeRelocate(0, 0, getWidth(), getHeight());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgentStylesheet() {
        return JFoenixResources.load("css/controls/jfx-tree-view-path.css").toExternalForm();
    }

    @Override
    protected double computeMinHeight(double width) {
        return super.computePrefHeight(width);
    }

    private Button createNextButton(TreeItem<JEVisTreeViewItem> temp) {
        String name = temp.getValue().toString();
        if (temp instanceof FilterableTreeItem filterableTreeItem) {
            name = filterableTreeItem.getValue().getObject().getName();
        }
        return new Button(name) {
            {
                setPadding(new Insets(getOffset(), 1.5 * getOffset(), getOffset(), 2 * getOffset()));
                setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            }

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                double width = getWidth();
                Polygon polygon = new Polygon();
                final double height = getHeight();
                polygon.getPoints().addAll(0.0, 0.0,
                        width - getOffset(), 0.0,
                        width, height / 2,
                        width - getOffset(), height,
                        0.0, height,
                        getOffset(), height / 2);
                setClip(polygon);

            }
        };
    }

    public Button createFirstButton(TreeItem<JEVisTreeViewItem> temp) {
        String name = temp.getValue().toString();
        if (temp instanceof FilterableTreeItem filterableTreeItem) {
            name = filterableTreeItem.getValue().getObject().getName();
        }
        return new Button(name) {
            {
                setPadding(new Insets(getOffset(), 1.5 * getOffset(), getOffset(), getOffset()));
                setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            }

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                double width = getWidth();
                Polygon polygon = new Polygon();
                final double height = getHeight();
                polygon.getPoints().addAll(0.0, 0.0,
                        width - getOffset(), 0.0,
                        width, height / 2,
                        width - getOffset(), height,
                        0.0, height);
                setClip(polygon);
            }
        };
    }

    private Button createLastButton(TreeItem<JEVisTreeViewItem> temp, TreeItem<JEVisTreeViewItem> parent) {
        String name = temp.getValue().toString();
        if (temp instanceof FilterableTreeItem filterableTreeItem) {
            name = filterableTreeItem.getValue().getObject().getName();
        }
        return new Button(name) {
            private final boolean noParent = parent == null;

            {
                setPadding(new Insets(getOffset(), getOffset(), getOffset(), (noParent ? 1 : 2) * getOffset()));
                setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            }

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                double width = getWidth();
                Polygon polygon = new Polygon();
                final double height = getHeight();
                polygon.getPoints().addAll(0.0, 0.0,
                        width, 0.0,
                        width, height,
                        0.0, height,
                        noParent ? 0 : getOffset(), noParent ? 0 : height / 2);
                setClip(polygon);
            }
        };
    }

    public double getOffset() {
        return offset.get();
    }

    public void setOffset(double offset) {
        this.offset.set(offset);
    }

    public StyleableDoubleProperty offsetProperty() {
        return offset;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<TreeViewPath, Number> OFFSET =
                new CssMetaData<TreeViewPath, Number>("-jfx-offset",
                        SizeConverter.getInstance(), 10.0) {
                    @Override
                    public boolean isSettable(TreeViewPath control) {
                        return !control.offset.isBound();
                    }

                    @Override
                    public StyleableDoubleProperty getStyleableProperty(TreeViewPath control) {
                        return control.offsetProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> CHILD_STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(ScrollPane.getClassCssMetaData());
            Collections.addAll(styleables, OFFSET);
            CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}

package org.jevis.jecc.plugin.dashboard.widget;

import eu.hansolo.fx.charts.data.PlotItem;
import eu.hansolo.fx.charts.event.ItemEventListener;
import eu.hansolo.fx.charts.tools.CtxBounds;
import eu.hansolo.fx.charts.tools.Helper;
import eu.hansolo.fx.charts.tools.Point;
import eu.hansolo.fx.geometry.Path;
import javafx.beans.DefaultProperty;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.stream.Collectors;


/**
 * User: hansolo
 * Date: 30.11.17
 * Time: 04:13
 */
@DefaultProperty("children")
public class SankeyPlot extends Region {
    private static final double PREFERRED_WIDTH = 600;
    private static final double PREFERRED_HEIGHT = 400;
    private static final double MINIMUM_WIDTH = 50;
    private static final double MINIMUM_HEIGHT = 50;
    private static final double MAXIMUM_WIDTH = 2048;
    private static final double MAXIMUM_HEIGHT = 2048;
    private static final Color DEFAULT_STREAM_COLOR = Color.rgb(164, 164, 164, 0.55);
    private static final Color DEFAULT_ITEM_COLOR = Color.rgb(164, 164, 164);
    private static final int DEFAULT_ITEM_WIDTH = 20;
    private static final int DEFAULT_NODE_GAP = 20;
    private static final double DEFAULT_OPACITY = 0.55;
    private double size;
    private double width;
    private double height;
    private Canvas canvas;
    private GraphicsContext ctx;
    private ObservableList<PlotItem> items;
    private ItemEventListener itemListener;
    private ListChangeListener<PlotItem> itemListListener;
    private Map<Integer, List<PlotItemData>> itemsPerLevel;
    private int minLevel;
    private int maxLevel;
    private double scaleY;
    private eu.hansolo.fx.charts.SankeyPlot.StreamFillMode _streamFillMode;
    private ObjectProperty<eu.hansolo.fx.charts.SankeyPlot.StreamFillMode> streamFillMode;
    private Color _streamColor;
    private ObjectProperty<Color> streamColor;
    private Color _textColor;
    private ObjectProperty<Color> textColor;
    private int _itemWidth;
    private IntegerProperty itemWidth;
    private boolean _autoItemWidth;
    private BooleanProperty autoItemWidth;
    private int _itemGap;
    private IntegerProperty itemGap;
    private boolean _autoItemGap;
    private BooleanProperty autoItemGap;
    private int _decimals;
    private IntegerProperty decimals;
    private boolean _showFlowDirection;
    private BooleanProperty showFlowDirection;
    private boolean _useItemColor;
    private BooleanProperty useItemColor;
    private Color _itemColor;
    private ObjectProperty<Color> itemColor;
    private double _connectionOpacity;
    private DoubleProperty connectionOpacity;
    private Locale _locale;
    private ObjectProperty<Locale> locale;
    private String formatString;
    private Map<Path, String> paths;
    private Tooltip tooltip;
    private Map<Integer, Integer> offsetMap = new HashMap<>();
    private double fontSize;
    // ******************** Constructors **************************************
    public SankeyPlot() {
        items = FXCollections.observableArrayList();
        itemListener = e -> redraw();
        itemListListener = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(addedItem -> addedItem.setOnItemEvent(itemListener));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(removedItem -> removedItem.removeItemEventListener(itemListener));
                }
            }
            prepareData();
        };

        itemsPerLevel = new LinkedHashMap<>();

        _streamFillMode = eu.hansolo.fx.charts.SankeyPlot.StreamFillMode.COLOR;
        _streamColor = DEFAULT_STREAM_COLOR;
        _textColor = Color.BLACK;
        _itemWidth = DEFAULT_ITEM_WIDTH;
        _autoItemWidth = true;
        _itemGap = DEFAULT_NODE_GAP;
        _autoItemGap = true;
        _decimals = 0;
        _showFlowDirection = false;
        _useItemColor = true;
        _itemColor = DEFAULT_ITEM_COLOR;
        _connectionOpacity = DEFAULT_OPACITY;
        _locale = Locale.getDefault();

        formatString = "%." + _decimals + "f";

        paths = new LinkedHashMap<>();

        initGraphics();
        registerListeners();
    }

    public Map<Integer, Integer> getOffsetMap() {
        return offsetMap;
    }

    public void setOffsetMap(Map<Integer, Integer> offsetMap) {
        this.offsetMap = offsetMap;
    }

    public void addOffset(int level, int offset) {
        offsetMap.put(level, offset);
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
        redraw();
    }

    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
                Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        tooltip = new Tooltip();
        tooltip.setAutoHide(true);

        getChildren().setAll(canvas);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        items.addListener(itemListListener);
        canvas.setOnMouseClicked(e -> {
            paths.forEach((path, tooltipText) -> {
                double eventX = e.getX();
                double eventY = e.getY();
                if (path.contains(eventX, eventY)) {
                    double tooltipX = eventX + canvas.getScene().getX() + canvas.getScene().getWindow().getX();
                    double tooltipY = eventY + canvas.getScene().getY() + canvas.getScene().getWindow().getY() - 25;
                    tooltip.setText(tooltipText);
                    tooltip.setX(tooltipX);
                    tooltip.setY(tooltipY);
                    tooltip.show(getScene().getWindow());
                }
            });
        });
    }

    // ******************** Methods *******************************************
    @Override
    public void layoutChildren() {
        super.layoutChildren();
    }

    @Override
    protected double computeMinWidth(final double HEIGHT) {
        return MINIMUM_WIDTH;
    }

    @Override
    protected double computeMinHeight(final double WIDTH) {
        return MINIMUM_HEIGHT;
    }

    @Override
    protected double computePrefWidth(final double HEIGHT) {
        return super.computePrefWidth(HEIGHT);
    }

    @Override
    protected double computePrefHeight(final double WIDTH) {
        return super.computePrefHeight(WIDTH);
    }

    @Override
    protected double computeMaxWidth(final double HEIGHT) {
        return MAXIMUM_WIDTH;
    }

    @Override
    protected double computeMaxHeight(final double WIDTH) {
        return MAXIMUM_HEIGHT;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public void dispose() {
        items.removeListener(itemListListener);
    }

    public List<PlotItem> getItems() {
        return items;
    }

    public void setItems(final PlotItem... ITEMS) {
        setItems(Arrays.asList(ITEMS));
    }

    public void setItems(final List<PlotItem> ITEMS) {
        items.setAll(ITEMS);
        prepareData();
    }

    public void addItem(final PlotItem ITEM) {
        if (!items.contains(ITEM)) {
            items.add(ITEM);
        }
        prepareData();
    }

    public void removeItem(final PlotItem ITEM) {
        if (items.contains(ITEM)) {
            items.remove(ITEM);
        }
        prepareData();
    }

    public eu.hansolo.fx.charts.SankeyPlot.StreamFillMode getStreamFillMode() {
        return null == streamFillMode ? _streamFillMode : streamFillMode.get();
    }

    public void setStreamFillMode(final eu.hansolo.fx.charts.SankeyPlot.StreamFillMode MODE) {
        if (null == streamFillMode) {
            _streamFillMode = MODE;
            redraw();
        } else {
            streamFillMode.set(MODE);
        }
    }

    public ObjectProperty<eu.hansolo.fx.charts.SankeyPlot.StreamFillMode> streamFillModeProperty() {
        if (null == streamFillMode) {
            streamFillMode = new ObjectPropertyBase<eu.hansolo.fx.charts.SankeyPlot.StreamFillMode>(_streamFillMode) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "streamFillMode";
                }
            };
            _streamFillMode = null;
        }
        return streamFillMode;
    }

    public Color getStreamColor() {
        return null == streamColor ? _streamColor : streamColor.get();
    }

    public void setStreamColor(final Color COLOR) {
        if (null == streamColor) {
            _streamColor = COLOR;
            redraw();
        } else {
            streamColor.set(COLOR);
        }
    }

    public ObjectProperty<Color> streamColorProperty() {
        if (null == streamColor) {
            streamColor = new ObjectPropertyBase<Color>(_streamColor) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "streamColor";
                }
            };
            _streamColor = null;
        }
        return streamColor;
    }

    public Color getTextColor() {
        return null == textColor ? _textColor : textColor.get();
    }

    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
            redraw();
        } else {
            textColor.set(COLOR);
        }
    }

    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) {
            textColor = new ObjectPropertyBase<Color>(_textColor) {
                @Override
                protected void invalidated() {
                    prepareData();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "textColor";
                }
            };
            _textColor = null;
        }
        return textColor;
    }

    public int getItemWidth() {
        return null == itemWidth ? _itemWidth : itemWidth.get();
    }

    public void setItemWidth(final int WIDTH) {
        if (null == itemWidth) {
            _itemWidth = Helper.clamp(2, 50, WIDTH);
            prepareData();
        } else {
            itemWidth.set(WIDTH);
        }
    }

    public IntegerProperty itemWidthProperty() {
        if (null == itemWidth) {
            itemWidth = new IntegerPropertyBase(_itemWidth) {
                @Override
                protected void invalidated() {
                    set(Helper.clamp(2, 50, get()));
                    prepareData();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "itemWidth";
                }
            };
        }
        return itemWidth;
    }

    public boolean isAutoItemWidth() {
        return null == autoItemWidth ? _autoItemWidth : autoItemWidth.get();
    }

    public void setAutoItemWidth(final boolean AUTO) {
        if (null == autoItemWidth) {
            _autoItemWidth = AUTO;
            prepareData();
        } else {
            autoItemWidth.set(AUTO);
        }
    }

    public BooleanProperty autoItemWidthProperty() {
        if (null == autoItemWidth) {
            autoItemWidth = new BooleanPropertyBase(_autoItemWidth) {
                @Override
                protected void invalidated() {
                    prepareData();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "autoItemWidth";
                }
            };
        }
        return autoItemWidth;
    }

    public int getItemGap() {
        return null == itemGap ? _itemGap : itemGap.get();
    }

    public void setItemGap(final int GAP) {
        if (null == itemGap) {
            _itemGap = Helper.clamp(0, 100, GAP);
            prepareData();
        } else {
            itemGap.set(GAP);
        }
    }

    public IntegerProperty itemGapProperty() {
        if (null == itemGap) {
            itemGap = new IntegerPropertyBase(_itemGap) {
                @Override
                protected void invalidated() {
                    set(Helper.clamp(0, 100, get()));
                    prepareData();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "itemGap";
                }
            };
        }
        return itemGap;
    }

    public boolean isAutoItemGap() {
        return null == autoItemGap ? _autoItemGap : autoItemGap.get();
    }

    public void setAutoItemGap(final boolean AUTO) {
        if (null == autoItemGap) {
            _autoItemGap = AUTO;
            prepareData();
        } else {
            autoItemGap.set(AUTO);
        }
    }

    public BooleanProperty autoItemGapProperty() {
        if (null == autoItemGap) {
            autoItemGap = new BooleanPropertyBase(_autoItemGap) {
                @Override
                protected void invalidated() {
                    prepareData();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "autoItemGap";
                }
            };
        }
        return autoItemGap;
    }

    public int getDecimals() {
        return null == decimals ? _decimals : decimals.get();
    }

    public void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals = Helper.clamp(0, 6, DECIMALS);
            formatString = new StringBuilder("%.").append(getDecimals()).append("f").toString();
            redraw();
        } else {
            decimals.set(DECIMALS);
        }
    }

    public IntegerProperty decimalsProperty() {
        if (null == decimals) {
            decimals = new IntegerPropertyBase(_decimals) {
                @Override
                protected void invalidated() {
                    set(Helper.clamp(0, 6, get()));
                    formatString = new StringBuilder("%.").append(get()).append("f").toString();
                    redraw();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "decimals";
                }
            };
        }
        return decimals;
    }

    public boolean getShowFlowDirection() {
        return null == showFlowDirection ? _showFlowDirection : showFlowDirection.get();
    }

    public void setShowFlowDirection(final boolean SHOW) {
        if (null == showFlowDirection) {
            _showFlowDirection = SHOW;
            redraw();
        } else {
            showFlowDirection.set(SHOW);
        }
    }

    public BooleanProperty showFlowDirectionProperty() {
        if (null == showFlowDirection) {
            showFlowDirection = new BooleanPropertyBase(_showFlowDirection) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "showFlowDirection";
                }
            };
        }
        return showFlowDirection;
    }

    public boolean getUseItemColor() {
        return null == useItemColor ? _useItemColor : useItemColor.get();
    }

    public void setUseItemColor(final boolean USE) {
        if (null == useItemColor) {
            _useItemColor = USE;
            redraw();
        } else {
            useItemColor.set(USE);
        }
    }

    public BooleanProperty useItemColorProperty() {
        if (null == useItemColor) {
            useItemColor = new BooleanPropertyBase(_useItemColor) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "useItemColor";
                }
            };
        }
        return useItemColor;
    }

    public Color getItemColor() {
        return null == itemColor ? _itemColor : itemColor.get();
    }

    public void setItemColor(final Color COLOR) {
        if (null == itemColor) {
            _itemColor = COLOR;
            redraw();
        } else {
            itemColor.set(COLOR);
        }
    }

    public ObjectProperty<Color> itemColorProperty() {
        if (null == itemColor) {
            itemColor = new ObjectPropertyBase<Color>(_itemColor) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "itemColor";
                }
            };
        }
        return itemColor;
    }

    public double getConnectionOpacity() {
        return null == connectionOpacity ? _connectionOpacity : connectionOpacity.get();
    }

    public void setConnectionOpacity(final double OPACITY) {
        if (null == connectionOpacity) {
            _connectionOpacity = Helper.clamp(0.1, 1.0, OPACITY);
            redraw();
        } else {
            connectionOpacity.set(OPACITY);
        }
    }

    public DoubleProperty connectionOpacityProperty() {
        if (null == connectionOpacity) {
            connectionOpacity = new DoublePropertyBase(_connectionOpacity) {
                @Override
                protected void invalidated() {
                    set(Helper.clamp(0.1, 1.0, get()));
                    redraw();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "connectionOpacity";
                }
            };
        }
        return connectionOpacity;
    }

    public Locale getLocale() {
        return null == locale ? _locale : locale.get();
    }

    public void setLocale(final Locale LOCALE) {
        if (null == locale) {
            _locale = LOCALE;
            prepareData();
        } else {
            locale.set(LOCALE);
        }
    }

    public ObjectProperty<Locale> localeProperty() {
        if (null == locale) {
            locale = new ObjectPropertyBase<Locale>(_locale) {
                @Override
                protected void invalidated() {
                    prepareData();
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "locale";
                }
            };
        }
        _locale = null;
        return locale;
    }

    public List<PlotItem> getItemsWithOnlyOutgoing() {
        //return getItems().stream().filter(PlotItem::hasOutgoing).filter(not(PlotItem::hasIncoming)).collect(Collectors.toList());
        return getItems().stream().filter(item -> item.hasOutgoing() && !item.hasIncoming()).collect(Collectors.toList());
    }

    public List<PlotItem> getItemsWithOnlyIncoming() {
        //return getItems().stream().filter(not(PlotItem::hasOutgoing)).filter(PlotItem::hasIncoming).collect(Collectors.toList());
        return getItems().stream().filter(item -> !item.hasOutgoing() && item.hasIncoming()).collect(Collectors.toList());
    }

    public List<PlotItem> getItemsWithInAndOutgoing() {
        return getItems().stream().filter(PlotItem::hasOutgoing).filter(PlotItem::hasIncoming).collect(Collectors.toList());
    }

    private void sortIncomingByOutgoingOrder(final List<PlotItem> INCOMING, final List<PlotItem> OUTGOING) {
        Collections.sort(INCOMING, Comparator.comparing(item -> OUTGOING.indexOf(item)));
    }

    private void sortOutgoingByNextLevelIncomingOrder(final List<PlotItem> OUTGOING, final List<PlotItem> INCOMING) {
        Collections.sort(OUTGOING, Comparator.comparing(item -> INCOMING.indexOf(item)));
    }

    private double getSumFromDataItems(final List<PlotItemData> DATA_ITEMS) {
        return DATA_ITEMS.stream().map(plotItemData -> plotItemData.getPlotItem()).mapToDouble(PlotItem::getMaxSum).sum();
    }

    private void prepareData() {
        // Split all items to levels
        itemsPerLevel.clear();
        items.forEach(item -> {
            int level = item.getLevel();
            if (itemsPerLevel.keySet().contains(level)) {
                itemsPerLevel.get(level).add(new PlotItemData(item));
            } else {
                itemsPerLevel.put(level, new ArrayList<>());
                itemsPerLevel.get(level).add(new PlotItemData(item));
            }
        });

        // Get min- and max level
        minLevel = itemsPerLevel.keySet().stream().mapToInt(Integer::intValue).min().getAsInt();
        maxLevel = itemsPerLevel.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();

        // Move items with no incoming streams to correct level dependent on level of their outgoing items
        List<PlotItemData> minLevelItems = itemsPerLevel.get(minLevel);
        Map<PlotItemData, Integer> itemsToMove = new LinkedHashMap<>();
        for (PlotItemData plotItemData : minLevelItems) {
            int minLevelOfOutgoingItems = maxLevel;
            int maxLevelOfOutgoingItems = minLevel;
            for (PlotItem plotItem : plotItemData.getPlotItem().getOutgoing().keySet()) {
                int levelOfItem = plotItem.getLevel();
                minLevelOfOutgoingItems = Math.min(minLevelOfOutgoingItems, levelOfItem);
                maxLevelOfOutgoingItems = Math.max(maxLevelOfOutgoingItems, levelOfItem);
            }
            if (minLevelOfOutgoingItems > minLevel + 1) {
                itemsToMove.put(plotItemData, minLevelOfOutgoingItems - 1);
            }
        }
        itemsToMove.forEach((itemData, newLevel) -> {
            minLevelItems.remove(itemData);
            //itemsPerLevel.get(newLevel).add(itemData);
            itemsPerLevel.get(newLevel).add(0, itemData);
        });

        // Reverse items in at each level
        itemsPerLevel.forEach((level, items) -> Collections.reverse(items));

        // Sort outgoing and incoming items at each level dependent on the order of former and next level items
        for (int level = minLevel; level <= maxLevel; level++) {
            List<PlotItemData> itemData = itemsPerLevel.get(level);
            if (level < maxLevel) {
                List<PlotItem> nextLevelItems = itemsPerLevel.get(level + 1).stream().map(plotItemData -> plotItemData.getPlotItem()).collect(Collectors.toList());
                itemData.forEach(id -> id.getPlotItem().sortOutgoingByGivenList(nextLevelItems));
            }
            if (level > minLevel) {
                List<PlotItem> formerLevelItems = itemsPerLevel.get(level - 1).stream().map(plotItemData -> plotItemData.getPlotItem()).collect(Collectors.toList());
                itemData.forEach(id -> id.getPlotItem().sortIncomingByGivenList(formerLevelItems));
            }
        }

        // Get max no of items, max sum of values etc.
        int maxNoOfItemsAtLevel = 0;
        double maxSumOfItemsAtLevel = 0;
        for (int i = minLevel; i <= maxLevel; i++) {
            List<PlotItemData> items = itemsPerLevel.get(i);
            maxNoOfItemsAtLevel = Math.max(maxNoOfItemsAtLevel, items.size());
            maxSumOfItemsAtLevel = Math.max(maxSumOfItemsAtLevel, getSumFromDataItems(items));
        }

        // Define drawing parameters
        double itemWidth = isAutoItemWidth() ? size * 0.025 : getItemWidth();
        double verticalGap = isAutoItemGap() ? size * 0.025 : getItemGap();
        double textGap = size * 0.0125;
        double maxSum = maxSumOfItemsAtLevel;
        int maxItems = maxNoOfItemsAtLevel;
        double horizontalGap = (width - itemWidth) / maxLevel;
        scaleY = (height - (maxItems - 1) * verticalGap) / maxSum;
        double spacerX;
        double spacerY;
        for (int level = minLevel; level <= maxLevel; level++) {
            spacerY = 0;
            spacerX = horizontalGap * level;
            for (PlotItemData itemData : itemsPerLevel.get(level)) {
                PlotItem item = itemData.getPlotItem();
                double itemHeight = item.getMaxSum() * scaleY;
                double textOffsetX = level < maxLevel ? textGap + itemWidth : -textGap;
                itemData.setBounds(spacerX, (height - itemHeight) - spacerY, itemWidth, itemHeight);
                itemData.setTextPoint(spacerX + textOffsetX, (height - itemHeight * 0.5) - spacerY);
                spacerY += itemHeight + verticalGap;
            }
        }

        createPaths();
        redraw();
    }

    // ******************** Resizing ******************************************
    private void resize() {
        width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            canvas.setWidth(width);
            canvas.setHeight(height);
            canvas.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            ctx.setTextBaseline(VPos.CENTER);
            ctx.setFont(Font.font(Helper.clamp(8, 24, size * 0.025)));

            prepareData();
        }
    }

    private void createPaths() {
        paths.clear();

        boolean showFlowDirection = getShowFlowDirection();
        double showDirectionOffsetX = size * 0.01875;
        double connectionOpacity = getConnectionOpacity();

        // Draw bezier curves between items
        for (int level = minLevel; level <= maxLevel; level++) {
            List<PlotItemData> itemDataInLevel = itemsPerLevel.get(level);
            int nextLevel = level + 1;

            // Go through all item data of the current level
            for (PlotItemData itemData : itemDataInLevel) {
                PlotItem item = itemData.getPlotItem();
//                if (level == 2) {
//                    itemData.setBounds(itemData.getBounds().getX(),itemData.getBounds().getY(),itemData.getBounds().getWidth()*0.8,itemData.getBounds().getHeight());
//                }


                CtxBounds bounds = itemData.getBounds();

                // Outgoing
                if (level < maxLevel) {
                    List<PlotItemData> nextLevelItemDataList = itemsPerLevel.get(nextLevel);
                    for (PlotItem outgoingItem : item.getOutgoing().keySet()) {
                        Optional<PlotItemData> targetItemDataOptional = nextLevelItemDataList.stream().filter(id -> id.getPlotItem().equals(outgoingItem)).findFirst();
                        if (!targetItemDataOptional.isPresent()) {
                            continue;
                        }


                        PlotItemData targetItemData = targetItemDataOptional.get();
//                        if (level == 2) {
//                            targetItemData.setBounds(targetItemData.getBounds().getX(),targetItemData.getBounds().getY(),targetItemData.getBounds().getWidth(),targetItemData.getBounds().getHeight());
//                        }
                        CtxBounds targetItemBounds = targetItemData.getBounds();
                        PlotItem targetItem = targetItemData.getPlotItem();

                        // Calculate y start position in target item dependent on item index in target incoming
                        double targetIncomingOffsetY = 0;
                        for (PlotItem incomingItem : targetItem.getIncoming().keySet()) {
                            if (incomingItem.equals(item)) {
                                break;
                            }
                            targetIncomingOffsetY += targetItem.getIncoming().get(incomingItem) * scaleY;
                        }

                        // Calculate the offset in x direction for the bezier curve control points
                        double targetBoundX = 0;
                        double sourceBoundX = 0;

                        for (Map.Entry<Integer, Integer> offsetEntry : offsetMap.entrySet()) {

                            //todo
                            /*
                            Move Boundaries

                            Source Boundary Offset of Offset from Level

                            Target Boundary Offset of Offset from Level +1

                             */
                            if (offsetEntry.getKey() == level && level != minLevel) {
                                sourceBoundX = offsetEntry.getValue();
                            }
                            if (offsetEntry.getKey() == level + 1 && level + 1 != maxLevel) {
                                targetBoundX = offsetEntry.getValue();
                            }

                        }


                        double ctrlPointOffsetX = (targetItemBounds.getMinX() - bounds.getMaxX()) * 0.25;


                        // Calculate the value of the current item in y direction
                        double outgoingValue = item.getOutgoing().get(outgoingItem);
                        double scaledValueY = outgoingValue * scaleY;

                        // Create Path
                        Path path = new Path();

                        // Set Gradient from current item to outgoing items
                        if (eu.hansolo.fx.charts.SankeyPlot.StreamFillMode.COLOR == getStreamFillMode()) {
                            path.setFill(getStreamColor());
                        } else {
                            path.setFill(new LinearGradient(0, 0, 1, 0,
                                    true, CycleMethod.NO_CYCLE,
                                    new Stop(0, Helper.getColorWithOpacity(item.getFill(), connectionOpacity)),
                                    new Stop(1, Helper.getColorWithOpacity(outgoingItem.getFill(), connectionOpacity))));
                        }

                        // Draw the bezier curve
                        path.moveTo(bounds.getMaxX() + sourceBoundX, bounds.getMinY() + itemData.getOutgoingOffsetY());
                        if (showFlowDirection) {
                            path.bezierCurveTo(bounds.getMaxX() + sourceBoundX + ctrlPointOffsetX, bounds.getMinY() + itemData.getOutgoingOffsetY(),
                                    targetItemBounds.getMinX() + targetBoundX - ctrlPointOffsetX, targetItemBounds.getMinY() + targetIncomingOffsetY,
                                    targetItemBounds.getMinX() + targetBoundX - showDirectionOffsetX, targetItemBounds.getMinY() + targetIncomingOffsetY);
                            path.lineTo(targetItemBounds.getMinX() + targetBoundX, targetItemBounds.getMinY() + targetIncomingOffsetY + scaledValueY * 0.5);
                            path.lineTo(targetItemBounds.getMinX() + targetBoundX - showDirectionOffsetX, targetItemBounds.getMinY() + targetIncomingOffsetY + scaledValueY);
                        } else {
                            path.bezierCurveTo(bounds.getMaxX() + ctrlPointOffsetX, bounds.getMinY() + itemData.getOutgoingOffsetY(),
                                    targetItemBounds.getMinX() - ctrlPointOffsetX, targetItemBounds.getMinY() + targetIncomingOffsetY,
                                    targetItemBounds.getMinX() + targetBoundX, targetItemBounds.getMinY() + targetIncomingOffsetY);
                            path.lineTo(targetItemBounds.getMinX() + targetBoundX, targetItemBounds.getMinY() + targetIncomingOffsetY + scaledValueY);
                        }

                        itemData.addToOutgoingOffset(scaledValueY);
                        targetItemData.addToIncomingOffset(scaledValueY);
                        path.bezierCurveTo(targetItemBounds.getMinX() + targetBoundX - ctrlPointOffsetX, targetItemBounds.getMinY() + targetIncomingOffsetY + scaledValueY,
                                bounds.getMaxX() + sourceBoundX + ctrlPointOffsetX, bounds.getMinY() + itemData.getOutgoingOffsetY(),
                                bounds.getMaxX() + sourceBoundX, bounds.getMinY() + itemData.getOutgoingOffsetY());
                        path.lineTo(bounds.getMaxX() + sourceBoundX, bounds.getMinY() + itemData.getOutgoingOffsetY());
                        path.closePath();

                        String tooltipText = new StringBuilder().append(item.getName())
                                .append(" -> ")
                                .append(targetItem.getName())
                                .append(" ")
                                .append(String.format(getLocale(), formatString, outgoingValue))
                                .toString();
                        paths.put(path, tooltipText);
                    }
                }
            }
        }
    }

    private void redraw() {
        ctx.clearRect(0, 0, width, height);
        paths.forEach((path, plotItem) -> {
            path.draw(ctx, true, false);
        });
        boolean useItemColor = getUseItemColor();
        Color itemColor = getItemColor();
        Color textColor = getTextColor();

        // Draw bezier curves between items
        for (int level = minLevel; level <= maxLevel; level++) {
            List<PlotItemData> itemDataInLevel = itemsPerLevel.get(level);

            // Go through all item data of the current level
            for (PlotItemData itemData : itemDataInLevel) {
                PlotItem item = itemData.getPlotItem();
                CtxBounds bounds = itemData.getBounds();

                // Draw item boxes with their labels
                ctx.setFill(useItemColor ? item.getFill() : itemColor);
                boolean rectMoved = false;

                //Todo
                /*
                set Text Font size for Item Name
                 */

                Font font = ctx.getFont();
                ctx.setFont(new Font(font.getName(), fontSize));


                //todo
                /*
                set offset for Rectangle (Item of Sankey Plot) and the Name

                if offset is not min or max level


                 */

                double offset = 0;

                ctx.setTextAlign(level == maxLevel ? TextAlignment.RIGHT : TextAlignment.LEFT);
                for (Map.Entry<Integer, Integer> offsetEntry : offsetMap.entrySet()) {
                    if (level == offsetEntry.getKey() && level != maxLevel && level != minLevel) {
                        offset = offsetEntry.getValue();
                    }
                }
                ctx.fillRect(bounds.getX() + offset, bounds.getY(), bounds.getWidth(), bounds.getHeight());
                ctx.setFill(textColor);
                ctx.fillText(item.getName(), itemData.getTextPoint().getX() + offset, itemData.getTextPoint().getY());

            }
        }
    }

    public enum StreamFillMode {COLOR, GRADIENT}

    // ******************** Inner Classes *************************************
    private class PlotItemData {
        private PlotItem plotItem;
        private CtxBounds bounds;           // bounds of the item rectangle
        private Point textPoint;        // point where text will be drawn
        private double incomingOffsetY;  // offset in y direction of already added incoming bezier curves
        private double outgoingOffsetY;  // offset in y direction of already added outgoing bezier curves
        private double value;


        // ******************** Constructors **********************************
        public PlotItemData(final PlotItem ITEM) {
            plotItem = ITEM;
            bounds = new CtxBounds();
            textPoint = new Point();
            incomingOffsetY = 0;
            outgoingOffsetY = 0;
            value = 0;
        }


        // ******************** Methods *******************************************
        public PlotItem getPlotItem() {
            return plotItem;
        }

        public CtxBounds getBounds() {
            return bounds;
        }

        public void setBounds(final double X, final double Y, final double WIDTH, final double HEIGHT) {
            bounds.set(X, Y, WIDTH, HEIGHT);
        }

        public Point getTextPoint() {
            return textPoint;
        }

        public void setTextPoint(final double X, final double Y) {
            textPoint.set(X, Y);
        }

        public double getIncomingOffsetY() {
            return incomingOffsetY;
        }

        public void setIncomingOffsetY(final double OFFSET) {
            incomingOffsetY = OFFSET;
        }

        public void addToIncomingOffset(final double ADD) {
            incomingOffsetY += ADD;
        }

        public void resetIncomingOffset() {
            incomingOffsetY = 0;
        }

        public double getOutgoingOffsetY() {
            return outgoingOffsetY;
        }

        public void setOutgoingOffsetY(final double OFFSET) {
            outgoingOffsetY = OFFSET;
        }

        public void addToOutgoingOffset(final double ADD) {
            outgoingOffsetY += ADD;
        }

        public void resetOutgoingOffset() {
            outgoingOffsetY = 0;
        }

        public double getValue() {
            return value;
        }

        public void setValue(final double VALUE) {
            value = VALUE;
        }
    }
}

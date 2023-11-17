package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.jevis.jeconfig.plugin.dashboard.config2.JsonNames.Widget.*;

/**
 * POJO to store the common Widget configuration in json
 */
public class WidgetPojo {

    private BorderWidths borderSize = new BorderWidths(0.2);
    private Color fontColor = Color.BLACK;
    private Color fontColorSecondary = Color.WHITESMOKE;
    private String title = "Title";
    private String tooltip = "";
    private Color backgroundColor = Color.web("#ffffff");
    private Double xPosition = 100d;
    private Double yPosition = 100d;
    private Size size = Size.DEFAULT;
    private Font font = Font.getDefault();
    private Double fontSize = 13d;
    private FontWeight fontWeight = FontWeight.NORMAL;
    private FontPosture fontPosture = FontPosture.REGULAR;
    private Boolean fontUnderlined = false;
    private Pos titlePosition = Pos.CENTER;
    private Boolean showShadow = true;
    private Integer decimals = 2;
    private String type = "";
    private Integer layer = 1;
    private boolean showValue = true;
    private boolean fixedTimeframe = false;

    private JsonNode dataHandlerJson;
    private static final Logger logger = LogManager.getLogger(WidgetPojo.class);
    private final JsonNode jsonNode;
    private int uuid = 0;

    public WidgetPojo() {
        this.jsonNode = JsonNodeFactory.instance.objectNode();
    }

    /**
     * Create new Config with uuid.
     *
     * @param uuid
     */
    public WidgetPojo(int uuid) {
        this.jsonNode = JsonNodeFactory.instance.objectNode();
        this.uuid = uuid;
    }

    public WidgetPojo(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
        try {
            try {
                this.title = jsonNode.get(TITLE).asText("");
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", TITLE, ex.getMessage());
            }

            try {
                this.tooltip = jsonNode.get(TOOLTIP).asText("");
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", TOOLTIP, ex.getMessage());
            }

            try {
                this.uuid = jsonNode.get(UUID).asInt(-1);
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", UUID, ex.getMessage());
            }

            try {

                /**
                 * Fallback, existing dashboards have only left,right,center
                 * but the POJO expects an Pos with center_right and so on.
                 **/
                if (jsonNode.get(TITLE_POSITION).asText().equalsIgnoreCase("left")) {
                    this.titlePosition = Pos.CENTER_LEFT;
                } else if (jsonNode.get(TITLE_POSITION).asText().equalsIgnoreCase("right")) {
                    this.titlePosition = Pos.CENTER_RIGHT;
                } else {
                    //correct implementation
                    this.titlePosition = Pos.valueOf(jsonNode.get(TITLE_POSITION).asText());
                }

            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", TITLE_POSITION, ex.getMessage());
            }


            try {
//                System.out.println("Parse Type: " + jsonNode.get(TYPE) + "     " + jsonNode);
                this.type = jsonNode.get(TYPE).asText("");
//                System.out.println("p: " + this.type);
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", "WidgetType", ex.getMessage());
            }

            try {
                this.backgroundColor = Color.valueOf(jsonNode.get(BACKGROUND_COLOR).asText(Color.TRANSPARENT.toString()));
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", BACKGROUND_COLOR, ex.getMessage());
            }

            try {
                this.fontColor = Color.valueOf(jsonNode.get(FONT_COLOR).asText(Color.BLACK.toString()));
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", FONT_COLOR, ex.getMessage());
            }
            try {
                this.size = new Size(jsonNode.get(HEIGHT).asDouble(25), jsonNode.get(WIDTH).asDouble(25));
            } catch (Exception ex) {
                logger.debug("Could not parse {} : {}", HEIGHT, ex.getMessage());
            }

            try {
                this.fontWeight = FontWeight.findByName(jsonNode.get(FONT_WEIGHT).asText());
            } catch (Exception ex) {
                logger.debug("Could not parse {} : {}", FONT_WEIGHT, ex.getMessage());
            }

            try {
                this.fontPosture = FontPosture.findByName(jsonNode.get(FONT_POSTURE).asText());
            } catch (Exception ex) {
                logger.debug("Could not parse {} : {}", FONT_POSTURE, ex.getMessage());
            }

            try {
                this.fontUnderlined = jsonNode.get(FONT_UNDERLINED).asBoolean();
            } catch (Exception ex) {
                logger.debug("Could not parse {} : {}", FONT_UNDERLINED, ex.getMessage());
            }

            try {
                this.fontSize = jsonNode.get(FONT_SIZE).asDouble(13);
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", FONT_SIZE, ex.getMessage());
            }

            try {
                this.xPosition = jsonNode.get(X_POS).asDouble(0);
                this.yPosition = jsonNode.get(Y_POS).asDouble(0);
            } catch (Exception ex) {
                logger.debug("Could not parse position: {}", ex.getMessage());
            }

            try {
                this.showShadow = jsonNode.get(SHOW_SHADOW).asBoolean(true);
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", SHOW_SHADOW, ex.getMessage());
            }

            try {
                this.showValue = jsonNode.get(SHOW_VALUE).asBoolean(true);
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", SHOW_VALUE, ex.getMessage());
            }

            try {
                this.borderSize = new BorderWidths(jsonNode.get(BORDER_SIZE).asDouble(0.2));
            } catch (Exception ex) {
                logger.debug("Could not parse position: {}", BORDER_SIZE, ex.getMessage());
            }

            try {
                this.decimals = jsonNode.get(DECIMALS).asInt(2);
            } catch (Exception ex) {
                logger.debug("Could not parse decimals: {}", DECIMALS, ex.getMessage());
            }

            try {
                this.layer = jsonNode.get(LAYER).asInt(1);
            } catch (Exception ex) {
                logger.debug("Could not parse position: {}", BORDER_SIZE, ex.getMessage());
            }

            try {
                this.fixedTimeframe = jsonNode.get(FIXED_TIMEFRAME).asBoolean(false);
            } catch (Exception ex) {
                logger.debug("Could not parse fixed timeframe: {}", BORDER_SIZE, ex.getMessage());
            }

            if (jsonNode.get(DATA_HANDLER_NODE) != null) {
                this.dataHandlerJson = jsonNode.get(DATA_HANDLER_NODE);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public boolean getShowValue() {
        return showValue;
    }

    public void setShowValue(boolean showValue) {
        this.showValue = showValue;
    }

    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public JsonNode getConfigNode(String name) {
        return this.jsonNode.get(name);
    }

    public BorderWidths getBorderSize() {
        return this.borderSize;
    }

    public void setBorderSize(BorderWidths borderSize) {
        this.borderSize = borderSize;
    }

    public Color getFontColor() {
        return this.fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public Color getFontColorSecondary() {
        return this.fontColorSecondary;
    }

    public void setFontColorSecondary(Color fontColorSecondary) {
        this.fontColorSecondary = fontColorSecondary;
    }

    public String getTitle() {
        return this.title;
    }

    public String getTooltip() {
        return this.tooltip;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Double getxPosition() {
        return this.xPosition;
    }

    public void setxPosition(Double xPosition) {
        this.xPosition = xPosition;
    }

    public Double getyPosition() {
        return this.yPosition;
    }

    public void setyPosition(Double yPosition) {
        this.yPosition = yPosition;
    }

    public Size getSize() {
        return this.size;
    }

    public void setSize(Size size) {
        if (size.getWidth() < 10) size.setWidth(10);
        if (size.getHeight() < 10) size.setHeight(10);
        this.size = size;
    }

    public Font getFont() {
        return this.font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Double getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(Double fontSize) {
        this.fontSize = fontSize;
    }

    public FontWeight getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(FontWeight fontWeight) {
        this.fontWeight = fontWeight;
    }

    public FontPosture getFontPosture() {
        return fontPosture;
    }

    public void setFontPosture(FontPosture fontPosture) {
        this.fontPosture = fontPosture;
    }

    public Boolean getFontUnderlined() {
        return fontUnderlined;
    }

    public void setFontUnderlined(Boolean fontUnderlined) {
        this.fontUnderlined = fontUnderlined;
    }

    public Pos getTitlePosition() {
        return this.titlePosition;
    }

    public void setTitlePosition(Pos titlePosition) {
        this.titlePosition = titlePosition;
    }

    public Boolean getShowShadow() {
        return this.showShadow;
    }

    public void setShowShadow(Boolean showShadow) {
        this.showShadow = showShadow;
    }

    public Integer getDecimals() {
        return this.decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLayer() {
        return layer;
    }

    public void setLayer(Integer layer) {
        this.layer = layer;
    }

    public boolean isFixedTimeframe() {
        return fixedTimeframe;
    }

    public void setFixedTimeframe(boolean fixedTimeframe) {
        this.fixedTimeframe = fixedTimeframe;
    }

    public WidgetPojo copy() {

        WidgetPojo copy = new WidgetPojo();
        copy.setxPosition(this.getxPosition());
        copy.setUuid(this.getUuid());
        copy.setTitle(this.getTitle());
        copy.setTitlePosition(this.getTitlePosition());
        copy.setBorderSize(this.getBorderSize());
        copy.setFont(this.getFont());
        copy.setTooltip(this.getTooltip());
        copy.setFontColorSecondary(this.getFontColorSecondary());
        copy.setBackgroundColor(this.getBackgroundColor());
        copy.setShowShadow(this.getShowShadow());
        copy.setSize(this.getSize());
        copy.setDecimals(this.getDecimals());
        copy.setFontSize(this.getFontSize());
        copy.setFontWeight(this.getFontWeight());
        copy.setFontPosture(this.getFontPosture());
        copy.setFontUnderlined(this.getFontUnderlined());
        copy.setType(this.getType());
        copy.setyPosition(this.getyPosition());
        copy.setLayer(this.getLayer());
        copy.setFixedTimeframe(this.isFixedTimeframe());

        return copy;
    }

    @Override
    public String toString() {
        return "WidgetPojo{" +
                "title='" + this.title + '\'' +
                ", xPosition=" + this.xPosition +
                ", yPosition=" + this.yPosition +
                ", size=" + this.size +
                ", type='" + this.type + '\'' +
                ", dezi='" + this.getDecimals() + '\'' +
                '}';
    }
}


package org.jevis.jecc.plugin.object.extension.revpi;

import com.fasterxml.jackson.annotation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "trend_id",
        "name"
})
@Generated("jsonschema2pojo")
public class RevPiTrend {

    @JsonProperty("id")
    private String trendId;
    @JsonProperty("name")
    private String name;

    @JsonProperty("config")
    private String config;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonIgnore
    private BooleanProperty selected = new SimpleBooleanProperty(this, "selected");

    @JsonProperty("id")
    public String getTrendId() {
        return trendId;
    }

    @JsonProperty("id")
    public void setTrendId(String trendId) {
        this.trendId = trendId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public String toString() {
        return "RevPiTrend{" +
                "trendId='" + trendId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @JsonProperty("config")
    public String getConfig() {
        return config;
    }

    @JsonProperty("config")
    public void setConfig(String config) {
        this.config = config;
    }
}


package org.jevis.jeconfig.plugin.object.extension.revpi;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

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
    private BooleanProperty selected = new SimpleBooleanProperty(this,"selected");

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

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
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

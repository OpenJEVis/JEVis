package org.jevis.revolutionpiserver;


import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "trend_id",
        "status",
        "value",
        "date_time"
})
@Generated("jsonschema2pojo")
public class RevPiResult {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("trend_id")
    private String trendId;
    @JsonProperty("status")
    private Integer status;
    @JsonProperty("value")
    private String value;
    @JsonProperty("date_time")
    private String dateTime;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("trend_id")
    public String getTrendId() {
        return trendId;
    }

    @JsonProperty("trend_id")
    public void setTrendId(String trendId) {
        this.trendId = trendId;
    }

    @JsonProperty("status")
    public Integer getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(Integer status) {
        this.status = status;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("date_time")
    public String getDateTime() {
        return dateTime;
    }

    @JsonProperty("date_time")
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "RevPiResult{" +
                "id=" + id +
                ", trendId=" + trendId +
                ", status=" + status +
                ", value='" + value + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
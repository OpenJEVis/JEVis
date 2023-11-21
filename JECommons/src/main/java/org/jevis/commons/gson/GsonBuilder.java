
package org.jevis.commons.gson;

import com.google.gson.*;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.hildan.fxgson.FxGson;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

public class GsonBuilder {

    public static com.google.gson.GsonBuilder createDefaultBuilder() {
        com.google.gson.GsonBuilder builder = FxGson.coreBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().serializeSpecialFloatingPointValues();
        registerDateTime(builder);
        registerSimpleListProperty(builder);
        return builder;
    }

    public static void registerDateTime(com.google.gson.GsonBuilder builder) {
        builder.registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
            @Override
            public JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json));
            }
        });
        builder.registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
            @Override
            public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                DateTime dt = ISODateTimeFormat.dateTime().parseDateTime(json.getAsString());
                return dt;
            }
        });
    }

    public static void registerSimpleListProperty(com.google.gson.GsonBuilder builder) {
        builder.registerTypeAdapter(SimpleListProperty.class, new JsonSerializer<SimpleListProperty>() {
            @Override
            public JsonElement serialize(SimpleListProperty listProperty, Type type, JsonSerializationContext jsonSerializationContext) {
                return new JsonPrimitive(listProperty.get().toString());
            }
        });

        builder.registerTypeAdapter(SimpleListProperty.class, new JsonDeserializer<SimpleListProperty>() {
            @Override
            public SimpleListProperty deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return new SimpleListProperty<>(FXCollections.observableArrayList(jsonElement));
            }
        });
    }
}

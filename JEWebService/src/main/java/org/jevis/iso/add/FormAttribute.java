/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.json.JsonType;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class FormAttribute {

    private String name = new String();
    private String transname = new String();
    private String value = new String();
    private String unit = new String();
    private File file = new File("");
    private Double doubleValue = 0.0;
    private Boolean booleanValue = false;
    private Long longValue = 0L;
    private String targetname = new String();
    private int IntValue = 0;
    private DateTime dateTimeValue = new DateTime();
    private List<ObjectTargetHelper> othelp = new ArrayList<>();
    private List<String> unithelp = new ArrayList<>();
    private FormAttributeType type = FormAttributeType.Text;

    public FormAttribute() {

    }

    public FormAttribute(SQLDataSource ds, JsonObject obj, String AttributeName, FormAttributeType AttributeType, JsonAttribute ja, JsonSample js) {

        this.setName(AttributeName);
        this.setType(AttributeType);

        int primitiveType = ja.getPrimitiveType();

        switch (primitiveType) {
            case JEVisConstants.PrimitiveType.STRING:
                //String

                String GuiDisplayType = "";
                for (JsonType jt : ds.getTypes(ds.getJEVisClass(obj.getJevisClass()))) {
                    if (jt.getName().equals(ja.getType())) {
                        GuiDisplayType = jt.getGuiType();
                    }
                }

                if (GuiDisplayType != null) {
                    switch (GuiDisplayType) {
                        case ("Text"):
                            //Simple String
                            if (js != null) {
                                this.setValue(js.getValue());
                            }
                            break;
                        case ("Text Area"):
                            //Text Field
                            if (js != null) {
                                this.setValue(js.getValue());
                            }
                            break;
                        case ("Password"):
                            //Text Password
                            if (js != null) {
                                this.setValue(js.getValue());
                            }
                            break;
                        case ("Date"):
                            //Date
                            if (js != null) {
                                if (!js.getValue().equals("") && !js.getValue().equals("NaN.NaN.NaN")) {
                                    DateTime dt = DateTime.parse(js.getValue(), DateTimeFormat.forPattern("dd.MM.yyyy"));
                                    this.setValue(dt.toString("yyyy-MM-dd"));
                                }
                            }
                            break;
                        case ("Date Time"):
                            if (js != null) {
                                DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
                                DateTime date = parser.parseDateTime(js.getValue());
                                this.setDateTimeValue(date);
                            }
                            break;
                        case ("Schedule"):
                            if (js != null) {
                                //...
                            }
                            break;
                        case ("Time Zone"):
                            if (js != null) {
                                //...
                            }
                            break;
                        case ("Object Target"):
                            if (js != null) {
                                this.setValue(js.getValue());
                                TargetHelper th = new TargetHelper(ds, ja);
                                if (th.hasObject() && th.isValid()) {
                                    this.setTargetname(th.getObject().getName());
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    if (js != null) {
                        this.setValue(js.getValue());
                    }
                }
                break;
            case JEVisConstants.PrimitiveType.DOUBLE:
                //Double
                //make choices for different type
                if (js != null) {
                    this.setDoubleValue(Double.parseDouble(js.getValue()));
                    this.setUnit(ja.getInputUnit().getLabel());
                }
                break;
            case JEVisConstants.PrimitiveType.LONG:
                //Long
                if (js != null) {
                    this.setLongValue(Long.parseLong(js.getValue()));
                }
                break;
            case JEVisConstants.PrimitiveType.FILE:
                //File
//                    if (js != null) {
//                        System.out.println("File found.");
//                        String filename = "";
//                        JEVisFile file = ds.getFile(obj.getId(), ja.getType(), null);
//                        filename = file.getFilename();
//                        System.out.println("Filename: " + filename);
//                        this.file = new File(filename);
//                        this.setValue(js.getTs());
//                    }
                break;
            case JEVisConstants.PrimitiveType.BOOLEAN:
                //Boolean
                if (js != null) {
                    this.setBooleanValue(Boolean.parseBoolean(js.getValue()));
                }
                break;
            case JEVisConstants.PrimitiveType.SELECTION:
                //Selection
                if (js != null) {
                    //...
                }
                break;
            case JEVisConstants.PrimitiveType.MULTI_SELECTION:
                //Multi Selection
                if (js != null) {
                    //...
                }
                break;
            case JEVisConstants.PrimitiveType.PASSWORD_PBKDF2:
                //Password PBKDF2
                if (js != null) {
                    //...
                }
                break;
            default:
                break;
        }

    }

    public FormAttribute(String AttributeName, FormAttributeType AttributeType) {
        this.setName(AttributeName);
        this.setType(AttributeType);
    }

    public FormAttribute(String AttributeName, FormAttributeType AttributeType, File AttributeFile) {
        this.setName(AttributeName);
        this.setType(AttributeType);
        this.setFile(AttributeFile);
    }

    public FormAttribute(String AttributeName, FormAttributeType AttributeType, String AttributeValue) {
        this.setName(AttributeName);
        this.setType(AttributeType);
        this.setValue(AttributeValue);
    }

    public FormAttribute(String AttributeName, FormAttributeType AttributeType, Double AttributeValue) {
        this.setName(AttributeName);
        this.setType(AttributeType);
        this.setDoubleValue(AttributeValue);
    }

    public FormAttribute(String AttributeName, FormAttributeType AttributeType, Boolean AttributeValue) {
        this.setName(AttributeName);
        this.setType(AttributeType);
        this.setBooleanValue(AttributeValue);
    }

    public FormAttribute(String AttributeName, FormAttributeType AttributeType, Long AttributeValue) {
        this.setName(AttributeName);
        this.setType(AttributeType);
        this.setLongValue(AttributeValue);
    }

    public FormAttribute(String AttributeName, FormAttributeType AttributeType, DateTime AttributeValue) {
        this.setName(AttributeName);
        this.setType(AttributeType);
        this.setDateTimeValue(AttributeValue);
    }

    public FormAttribute(SQLDataSource ds, JsonObject obj, String type, FormAttributeType output, JsonAttribute att) throws JEVisException, IOException {
        this.setName(att.getType());
        this.setType(output);
        JEVisFile file = ds.getFile(ds, obj.getId(), att.getType());
        if (Objects.nonNull(file)) {
            String filename = file.getFilename();
            this.file = new File(filename);
        }
        this.setValue(att.getEnds());
    }

    public int getIntValue() {
        return IntValue;
    }

    public void setIntValue(int IntValue) {
        this.IntValue = IntValue;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public FormAttributeType getType() {
        return type;
    }

    public void setType(FormAttributeType type) {
        this.type = type;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransname() {
        return transname;
    }

    public void setTransname(String transname) {
        this.transname = transname;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<ObjectTargetHelper> getOthelp() {
        return othelp;
    }

    public void setOthelp(List<ObjectTargetHelper> othelp) {
        this.othelp = othelp;
    }

    public List<String> getUnithelp() {
        return unithelp;
    }

    public void setUnithelp(List<String> unithelp) {
        this.unithelp = unithelp;
    }

    public String getTargetname() {
        return targetname;
    }

    public void setTargetname(String targetname) {
        this.targetname = targetname;
    }

    public DateTime getDateTimeValue() {
        return dateTimeValue;
    }

    public void setDateTimeValue(DateTime dateTimeValue) {
        this.dateTimeValue = dateTimeValue;
    }

    @Override
    public String toString() {
        return "FormAttribute{" + "name=" + name + ", file=" + file + ", doubleValue=" + doubleValue + ", booleanValue=" + booleanValue + ", longValue=" + longValue + ", IntValue=" + IntValue + ", dateTimeValue=" + dateTimeValue + ", othelp=" + othelp + ", value=" + value + ", type=" + type + '}';
    }

    public enum FormAttributeType {
        StringEnum, Text, Double, Long, File, Boolean, Selection, Date, DateTime, Schedule, TimeZone, TextArea, Password, TextPassword, MultiSelection, ObjectTarget, AttributeTarget
    }

}

package org.jevis.jsonparser;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Parser;
import org.jevis.commons.driver.ParserReport;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JEVisJSONParser implements Parser {
    List<JEVisObject> jsonChannels = new ArrayList<>();
    JEVisObject parserObject;
    List<Result> results = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(JEVisJSONParser.class);

    @Override
    public void initialize(JEVisObject parserObject) {
        this.parserObject = parserObject;
        try {
            jsonChannels.addAll(getChannels(parserObject));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void parse(List<InputStream> input, DateTimeZone timezone) {
        try {
            if (!parserObject.getAttribute("Date Time Path").hasSample()) return;
            String dateTimePath = parserObject.getAttribute("Date Time Path").getLatestSample().getValueAsString();
            input.forEach(inputStream -> {
                JSONParser jsonParser = new JSONParser(inputStream);
                List<DateTime> dateTimes = new ArrayList<>();
                try {
                    if (!parserObject.getAttribute("Date Time Format").hasSample()) return;
                    String dateTimeFormat = parserObject.getAttribute("Date Time Format").getLatestSample().getValueAsString();
                    dateTimes = convertDateTime(dateTimePath, jsonParser, dateTimeFormat);
                } catch (Exception e) {
                    logger.error(e);
                }
                List<DateTime> finalDateTimes = dateTimes;
                jsonChannels.forEach(jsonChannel -> {
                    try {
                        if (!jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.DATA_POINT_PATH).hasSample()) return;
                        String valuePath = jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.DATA_POINT_PATH).getLatestSample().getValueAsString();
                        List<String> statusValues = getStatusList(jsonParser, jsonChannel);
                        String statusOK = getStatusOkCondition(jsonChannel, DataCollectorTypes.Channel.JSONChannel.STAUS_VALUE_OK);
                        String regex = getRegex(jsonChannel);
                        List<String> values = getValueList(jsonParser, valuePath, regex);
                        String targetString = getTargetValueAtribute(jsonChannel);
                        Map map = getDateValueMap(finalDateTimes, values, statusValues, statusOK);
                        results.addAll(getResults(targetString, map));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private List<String> getValueList(JSONParser jsonParser, String valuePath, String regex) {
        List<JsonNode> valuesJson = jsonParser.parse(valuePath);
        List<String> values = convertValues(valuesJson, regex);
        return values;
    }

    private static String getStatusOkCondition(JEVisObject jsonChannel, String stausValueOk) throws JEVisException {
        String statusOK = null;
        if (jsonChannel.getAttribute(stausValueOk).hasSample()) {
            statusOK = jsonChannel.getAttribute(stausValueOk).getLatestSample().getValueAsString();
        }
        return statusOK;
    }

    private static List<String> getStatusList(JSONParser jsonParser, JEVisObject jsonChannel) throws JEVisException {
        String statusPath;
        List<String> statusValues = null;
        if (jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.STATUS_PATH).hasSample()) {
            statusPath = jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.STATUS_PATH).getLatestSample().getValueAsString();
            statusValues = jsonParser.parse(statusPath).stream().map(jsonNode -> jsonNode.asText()).collect(Collectors.toList());
        }
        return statusValues;
    }

    private static List<Result> getResults(String targetString, Map<DateTime,String> map) {
        List<Result> resultList = map.entrySet().stream().map(dateTimeStringEntry -> new Result(targetString, dateTimeStringEntry.getValue(), dateTimeStringEntry.getKey())).collect(Collectors.toList());
        return resultList;
    }

    private static String getRegex(JEVisObject jsonChannel) throws JEVisException {
        String regex = getStatusOkCondition(jsonChannel, DataCollectorTypes.Channel.JSONChannel.REGEX);
        return regex;
    }

    private List<String> convertValues(List<JsonNode> valuesJson, String regexPattern) {
        Pattern pattern = Pattern.compile(regexPattern);
        if (regexPattern.isEmpty() || regexPattern == null)
            return valuesJson.stream().map(jsonNode -> jsonNode.asText()).collect(Collectors.toList());

        List<String> stringValues = valuesJson.stream().map(jsonNode -> {
            Matcher m = pattern.matcher(jsonNode.asText());
            if (m.find()) {
                return m.group();
            } else {
                return null;
            }
        }).collect(Collectors.toList());

        return stringValues;


    }

    private static Map<DateTime, String> getDateValueMap(List<DateTime> finalDateTimeList, List<String> valueList, List<String> stausList, String OK) {
        Map<DateTime, String> map;
        if (stausList == null) {
            map = IntStream.range(0, finalDateTimeList.size())
                    .boxed()
                    .collect(Collectors.toMap(i -> finalDateTimeList.get(i), i -> valueList.get(i)));

        } else {
            map = IntStream.range(0, finalDateTimeList.size())
                    .filter(value -> {
                        if (stausList.get(value).equals(OK)) return true;
                        else return false;
                    })
                    .boxed()
                    .collect(Collectors.toMap(i -> finalDateTimeList.get(i), i -> valueList.get(i)));

        }


        return map;
    }

    private List<DateTime> convertDateTime(String dateTimePath, JSONParser jsonParser, String dateTimeFormat) {

        List<JsonNode> dateTimesJSON = jsonParser.parse(dateTimePath);
        DateTimeFormatter FMT = DateTimeFormat.forPattern(dateTimeFormat);
        List<DateTime> dateTimes = dateTimesJSON.stream().map(jsonNode -> FMT.parseDateTime(jsonNode.asText())).collect(Collectors.toList());
        return dateTimes;
    }

    private String getTargetValueAtribute(JEVisObject jsonChannel) throws JEVisException {
        JEVisClass channelClass = jsonChannel.getDataSource().getJEVisClass(DataCollectorTypes.Channel.JSONChannel.NAME);
        JEVisType targetIdType = channelClass.getType(DataCollectorTypes.Channel.JSONChannel.TARGETID);
        String targetString = DatabaseHelper.getObjectAsString(jsonChannel, targetIdType);
        return targetString;
    }

    @Override
    public List<Result> getResult() {
        return results;
    }

    @Override
    public ParserReport getReport() {
        return null;
    }

    public List<JEVisObject> getChannels(JEVisObject jeVisObject) {
        List<JEVisObject> channels = new ArrayList<>();
        try {
            JEVisClass channelDirClass = jeVisObject.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.JSONChannelDirectory.NAME);
            JEVisClass channelClass = jeVisObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.JSONChannel.NAME);
            jeVisObject.getChildren(channelDirClass, false).forEach(dir -> {
                channels.addAll(getChannels(dir));
            });
            List<JEVisObject> channelsToBeAdded = jeVisObject.getChildren(channelClass, false);
            logger.debug("Added Channels to List {}", channelsToBeAdded);
            channels.addAll(channelsToBeAdded);
        } catch (Exception e) {
            logger.error(e);
        }
        return channels;

    }
}

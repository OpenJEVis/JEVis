package org.jevis.jsonparser;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.classes.JC;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Parser;
import org.jevis.commons.driver.ParserReport;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JEVisJSONParser implements Parser {
    private static final Logger logger = LogManager.getLogger(JEVisJSONParser.class);
    List<JEVisObject> jsonChannels = new ArrayList<>();
    JEVisObject parserObject;
    List<Result> results = new ArrayList<>();

    private static String getStatusOkCondition(JEVisObject jsonChannel, String stausValueOk) throws JEVisException {
        String statusOK = null;
        if (jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.STAUS_VALUE_OK).hasSample()) {
            statusOK = jsonChannel.getAttribute(stausValueOk).getLatestSample().getValueAsString();
        }
        return statusOK;
    }

    private static List<String> getStatusList(JSONParser jsonParser, JEVisObject jsonChannel) throws JEVisException {
        String statusPath;
        List<String> statusValues = null;
        if (jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.STATUS_PATH).hasSample()) {
            statusPath = jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.STATUS_PATH).getLatestSample().getValueAsString();
            statusValues = jsonParser.parse(statusPath).stream().map(JsonNode::asText).collect(Collectors.toList());
        }
        return statusValues;
    }

    private static List<Result> getResults(String targetString, Map<DateTime, ?> map) {
        List<Result> resultList = map.entrySet().stream().map(dateTimeStringEntry -> new Result(targetString, dateTimeStringEntry.getValue(), dateTimeStringEntry.getKey())).collect(Collectors.toList());
        return resultList;
    }

    private static String getRegex(JEVisObject jsonChannel) throws JEVisException {
        String regex = getStatusOkCondition(jsonChannel, DataCollectorTypes.Channel.JSONChannel.REGEX);
        return regex;
    }

    private static Map<DateTime, ?> getDateValueMap(List<DateTime> finalDateTimeList, List<?> valueList, List<String> stausList, String OK) {
        Map<DateTime, ?> map;
        if (stausList == null) {
            map = IntStream.range(0, finalDateTimeList.size())
                    .boxed()
                    .collect(Collectors.toMap(finalDateTimeList::get, valueList::get));

        } else {
            map = IntStream.range(0, finalDateTimeList.size())
                    .filter(value -> {
                        return stausList.get(value).equals(OK);
                    })
                    .boxed()
                    .collect(Collectors.toMap(finalDateTimeList::get, valueList::get));

        }


        return map;
    }

    @Override
    public void initialize(JEVisObject parserObject) {

        this.parserObject = parserObject;
        try {
            jsonChannels.addAll(getChannels(parserObject));
        } catch (Exception e) {
            logger.error("Could not initialize parser object", e);
        }

    }

    @Override
    public void parse(List<InputStream> input, DateTimeZone timezone) {
        try {

            if (!parserObject.getAttribute(JC.Parser.JSONParser.a_dateTimePath).hasSample()) return;
            String dateTimePath = parserObject.getAttribute(JC.Parser.JSONParser.a_dateTimePath).getLatestSample().getValueAsString();
            input.forEach(inputStream -> {
                JSONParser jsonParser = new JSONParser(inputStream);
                List<DateTime> dateTimes = new ArrayList<>();
                try {
                    if (!parserObject.getAttribute(JC.Parser.JSONParser.a_dateTimeFormat).hasSample()) return;
                    dateTimes = getDateTimes(dateTimePath, jsonParser, dateTimes);
                } catch (Exception e) {
                    logger.error(e);
                }
                List<DateTime> finalDateTimes = dateTimes;
                jsonChannels.forEach(jsonChannel -> {
                    try {
                        if (!jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.DATA_POINT_PATH).hasSample())
                            return;
                        List<Result> resultList = getResults(jsonParser, finalDateTimes, jsonChannel);
                        results.addAll(resultList);
                    } catch (Exception e) {
                        logger.error("Could not get results ", e);
                    }
                });
            });
        } catch (Exception e) {
            logger.error("Could not parse object ", e);
        }


    }

    private List<DateTime> getDateTimes(String dateTimePath, JSONParser jsonParser, List<DateTime> dateTimes) throws JEVisException {
        String dateTimeFormat = parserObject.getAttribute(JC.Parser.JSONParser.a_dateTimeFormat).getLatestSample().getValueAsString();
        dateTimes = convertDateTime(dateTimePath, jsonParser, dateTimeFormat);
        return dateTimes;
    }

    private List<Result> getResults(JSONParser jsonParser, List<DateTime> finalDateTimes, JEVisObject jsonChannel) throws JEVisException {
        String valueFormat = jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.VALUE_FORMAT).hasSample() ?
                jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.VALUE_FORMAT).getLatestSample().getValueAsString() :
                "String";
        if (!jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.DATA_POINT_PATH).hasSample())
            return new ArrayList<>();
        String valuePath = jsonChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.DATA_POINT_PATH).getLatestSample().getValueAsString();
        List<String> statusValues = getStatusList(jsonParser, jsonChannel);
        String statusOK = getStatusOkCondition(jsonChannel, DataCollectorTypes.Channel.JSONChannel.STAUS_VALUE_OK);
        String regex = getRegex(jsonChannel);
        List<?> values = getValueList(jsonParser, valuePath, regex, valueFormat);
        String targetString = getTargetValueAttribute(jsonChannel);
        Map map = getDateValueMap(finalDateTimes, values, statusValues, statusOK);
        List<Result> resultList = getResults(targetString, map);
        return resultList;
    }

    private List<?> getValueList(JSONParser jsonParser, String valuePath, String regex, String valueFormat) {
        List<JsonNode> valuesJson = jsonParser.parse(valuePath);
        List<String> stringValues = regexValues(valuesJson, regex);
        logger.debug("Value format: {}", valueFormat);
        List<?> values = convertValues(stringValues, valueFormat);
        return values;
    }

    private List<String> regexValues(List<JsonNode> valuesJson, String regexPattern) {
        if (regexPattern == null || regexPattern.isEmpty())
            return valuesJson.stream().map(JsonNode::asText).collect(Collectors.toList());
        Pattern pattern = Pattern.compile(regexPattern);
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

    private List<?> convertValues(List<String> strings, String valueClass) {
        logger.debug("Converter values {}", strings);
        logger.debug("Converter value class {}", valueClass);

        if (valueClass.equals("Double")) {
            return strings.stream().map(s -> {
                try {
                    return Double.valueOf(s);
                } catch (Exception e) {
                    logger.error("could not parse {}", s);
                    logger.error(e);
                    return 0;
                }

            }).collect(Collectors.toList());
        } else if (valueClass.equals("String") || valueClass.isEmpty()) {
            return strings;
        } else if (valueClass.equals("Boolean")) {
            return strings.stream().map(s -> {
                try {
                    return Boolean.valueOf(s);
                } catch (Exception e) {
                    logger.error("Could not parse {}", s);
                    logger.error(e);
                    return false;
                }
            }).collect(Collectors.toList());
        }
        return strings;


    }

    private List<DateTime> convertDateTime(String dateTimePath, JSONParser jsonParser, String dateTimeFormat) {

        List<JsonNode> dateTimesJSON = jsonParser.parse(dateTimePath);
        DateTimeFormatter FMT = DateTimeFormat.forPattern(dateTimeFormat);
        List<DateTime> dateTimes = dateTimesJSON.stream().map(jsonNode -> FMT.parseDateTime(jsonNode.asText())).collect(Collectors.toList());
        return dateTimes;
    }

    private String getTargetValueAttribute(JEVisObject jsonChannel) throws JEVisException {
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

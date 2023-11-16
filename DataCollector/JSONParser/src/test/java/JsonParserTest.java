import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.classes.JC;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.jsonparser.JEVisJSONParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javolution.testing.TestContext.assertEquals;
import static javolution.testing.TestContext.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class JsonParserTest {

    @Spy
    JEVisJSONParser jsonParser = new JEVisJSONParser();

    @Mock
    JEVisObject parserJEVisObject;

    @Mock
    JEVisAttribute a_dateTimePath;

    @Mock
    JEVisObject jevisObejctChannel;
    @Mock
    JEVisAttribute a_dataPointPath;
    @Mock
    JEVisAttribute a_dateTimeFormat;
    @Mock
    JEVisAttribute a_valueFormat;
    @Mock
    JEVisAttribute a_statusOk;
    @Mock
    JEVisAttribute a_stausPath;

    @Mock
    JEVisSample date_timeSample;
    @Mock
    JEVisSample dateTimeFormatSample;

    @Mock
    JEVisSample dataPointPathSample;

    @Mock
    JEVisSample valueFormatSample;

    @Mock
    JEVisDataSource jeVisDataSource;

    @Mock
    JEVisClass channelClass;

    @Mock
    JEVisType targetIdType;

    @Spy
    DatabaseHelper databaseHelper;

    @BeforeAll
    public static void initStaticMock() throws JEVisException {
    mockStatic(DatabaseHelper.class);
    when(DatabaseHelper.getObjectAsString(Mockito.any(JEVisObject.class), Mockito.any(JEVisType.class))).thenReturn("test");
    }
    @BeforeEach

    public void initMockParserObject() throws JEVisException {
        MockitoAnnotations.initMocks(this);
        Mockito.when(parserJEVisObject.getAttribute(JC.Parser.JSONParser.a_dateTimePath)).thenReturn(a_dateTimePath);
        Mockito.when(a_dateTimePath.hasSample()).thenReturn(true);
        Mockito.when(a_dateTimePath.getLatestSample()).thenReturn(date_timeSample);


        Mockito.when(parserJEVisObject.getAttribute(JC.Parser.JSONParser.a_dateTimeFormat)).thenReturn(a_dateTimeFormat);
        Mockito.when(a_dateTimeFormat.hasSample()).thenReturn(true);
        Mockito.when(a_dateTimeFormat.getLatestSample()).thenReturn(dateTimeFormatSample);


        List<JEVisObject> channels = new ArrayList<>();
        channels.add(jevisObejctChannel);

        Mockito.when(jsonParser.getChannels(parserJEVisObject)).thenReturn(channels);
        Mockito.when(jsonParser.getChannels(parserJEVisObject)).thenReturn(channels);


        Mockito.when(jevisObejctChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.STATUS_PATH)).thenReturn(a_stausPath);
        Mockito.when(jevisObejctChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.DATA_POINT_PATH)).thenReturn(a_dataPointPath);
        Mockito.when(jevisObejctChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.VALUE_FORMAT)).thenReturn(a_valueFormat);
        Mockito.when(jevisObejctChannel.getAttribute(DataCollectorTypes.Channel.JSONChannel.STAUS_VALUE_OK)).thenReturn(a_statusOk);

        Mockito.when(a_stausPath.hasSample()).thenReturn(false);
        Mockito.when(a_dataPointPath.hasSample()).thenReturn(true);
        Mockito.when(a_valueFormat.hasSample()).thenReturn(true);
        Mockito.when(a_statusOk.hasSample()).thenReturn(false);

        Mockito.when(a_dataPointPath.getLatestSample()).thenReturn(dataPointPathSample);

        Mockito.when(a_valueFormat.getLatestSample()).thenReturn(valueFormatSample);
        Mockito.when(valueFormatSample.getValueAsString()).thenReturn("Double");

        Mockito.when(jevisObejctChannel.getDataSource()).thenReturn(jeVisDataSource);
        Mockito.when(jeVisDataSource.getJEVisClass(DataCollectorTypes.Channel.JSONChannel.NAME)).thenReturn(channelClass);
        Mockito.when(channelClass.getType(DataCollectorTypes.Channel.JSONChannel.TARGETID)).thenReturn(targetIdType);






    }


    @Test
    void jsonResultSizeTest1_should100() {
        try {
            Mockito.when(date_timeSample.getValueAsString()).thenReturn("date_time");
            Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd HH:mm:ss");
            Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("value");

            jsonParser.initialize(parserJEVisObject);
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test.json"));
            List<InputStream> inputStreams = new ArrayList<>();
            inputStreams.add(inputStream);
            jsonParser.parse(inputStreams, DateTimeZone.UTC);

            assertEquals(1000, jsonParser.getResult().size());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    void jsonResultSizeTest2_should383() {
        try {
            Mockito.when(date_timeSample.getValueAsString()).thenReturn("5344.data.ts");
            Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("5344.data.v");
            Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            jsonParser.initialize(parserJEVisObject);
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test2.json"));
            List<InputStream> inputStreams = new ArrayList<>();
            inputStreams.add(inputStream);
            jsonParser.parse(inputStreams, DateTimeZone.UTC);

            assertEquals(383, jsonParser.getResult().size());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    void jsonResultSizeTest3_should383() throws JEVisException, FileNotFoundException {
        try {
            Mockito.when(date_timeSample.getValueAsString()).thenReturn("timestamp");
            Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("value");
            Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            jsonParser.initialize(parserJEVisObject);
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
            List<InputStream> inputStreams = new ArrayList<>();
            inputStreams.add(inputStream);
            jsonParser.parse(inputStreams, DateTimeZone.UTC);

            assertEquals(383, jsonParser.getResult().size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    void JsonResult_hasResultAtDateBetween() {
        try {
        Mockito.when(date_timeSample.getValueAsString()).thenReturn("timestamp");
        Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("value");
        Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        jsonParser.initialize(parserJEVisObject);
        InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
        List<InputStream> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream);
        jsonParser.parse(inputStreams, DateTimeZone.UTC);

            DateTime start = new DateTime(2023, 1, 1, 1, 00, DateTimeZone.forOffsetHours(1));
            DateTime end = new DateTime(2023, 01, 05, 1, 00, DateTimeZone.forOffsetHours(1));

            assertTrue(jsonParser.getResult().stream().map(result -> result.getDate()).filter(dateTime -> dateTime.isAfter(start) && dateTime.isBefore(end)).count() == 383);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    void JsonResult_hasValue_1_83() {


        try {
            Mockito.when(date_timeSample.getValueAsString()).thenReturn("timestamp");
            Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("value");
            Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            jsonParser.initialize(parserJEVisObject);
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
            List<InputStream> inputStreams = new ArrayList<>();
            inputStreams.add(inputStream);
            jsonParser.parse(inputStreams, DateTimeZone.UTC);
            assertTrue(jsonParser.getResult().stream().map(result -> result.getValue()).collect(Collectors.toList()).contains(1.1833333333333376));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    void JsonResult_hasValue_1_41() {


        try {
            Mockito.when(date_timeSample.getValueAsString()).thenReturn("timestamp");
            Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("value");
            Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            jsonParser.initialize(parserJEVisObject);
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
            List<InputStream> inputStreams = new ArrayList<>();
            inputStreams.add(inputStream);
            jsonParser.parse(inputStreams, DateTimeZone.UTC);

            assertTrue(jsonParser.getResult().stream().map(result -> result.getValue()).collect(Collectors.toList()).contains(1.41013888888889));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    void JsonResult_hasValue_1_00() {


        try {
            Mockito.when(date_timeSample.getValueAsString()).thenReturn("timestamp");
            Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("value");
            Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            jsonParser.initialize(parserJEVisObject);
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
            List<InputStream> inputStreams = new ArrayList<>();
            inputStreams.add(inputStream);
            jsonParser.parse(inputStreams, DateTimeZone.UTC);

            assertTrue(jsonParser.getResult().stream().map(result -> result.getValue()).collect(Collectors.toList()).contains(1.0097222222222224));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    void JsonResult_hasValue_0_25() {


        try {
            Mockito.when(date_timeSample.getValueAsString()).thenReturn("timestamp");
            Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("value");
            Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            jsonParser.initialize(parserJEVisObject);
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
            List<InputStream> inputStreams = new ArrayList<>();
            inputStreams.add(inputStream);
            jsonParser.parse(inputStreams, DateTimeZone.UTC);


            assertTrue(jsonParser.getResult().stream().map(result -> result.getValue()).collect(Collectors.toList()).contains(0.25));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    void JsonResult_hasValue_1_07() {


        try {
            Mockito.when(date_timeSample.getValueAsString()).thenReturn("timestamp");
            Mockito.when(dataPointPathSample.getValueAsString()).thenReturn("value");
            Mockito.when(dateTimeFormatSample.getValueAsString()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            jsonParser.initialize(parserJEVisObject);
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
            List<InputStream> inputStreams = new ArrayList<>();
            inputStreams.add(inputStream);
            jsonParser.parse(inputStreams, DateTimeZone.UTC);


            assertTrue(jsonParser.getResult().stream().map(result -> result.getValue()).collect(Collectors.toList()).contains(1.0733333333333326));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

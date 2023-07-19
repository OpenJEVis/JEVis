import com.fasterxml.jackson.databind.JsonNode;
import org.jevis.jsonparser.JSONParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javolution.testing.TestContext.assertEquals;
import static javolution.testing.TestContext.assertTrue;

public class JsonParserTest

{

    private static Map<DateTime,Double> nodes1;
    private static Map<DateTime,Double> nodes2;
    private static Map<DateTime,Double> nodes3;

    private static DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static DateTimeFormatter FMT2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");


    @BeforeAll

    static void init() {
        try {
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test.json"));
            JSONParser jsonParser = new JSONParser(inputStream);
            InputStream inputStream2 = new FileInputStream(new File("src/test/resources/test2.json"));
            JSONParser jsonParser2 = new JSONParser(inputStream2);
            InputStream inputStream3 = new FileInputStream(new File("src/test/resources/test3.json"));
            JSONParser jsonParser3 = new JSONParser(inputStream3);


            nodes1 = IntStream.range(0, jsonParser.parse("date_time").size())
                    .boxed()
                    .collect(Collectors.toMap(i ->  FMT2.parseDateTime(jsonParser.parse("date_time").get(i).asText()), i ->  jsonParser.parse("value").get(i).asDouble()));


            nodes2 = IntStream.range(0, jsonParser2.parse("5344.data.ts").size())
                    .boxed()
                    .collect(Collectors.toMap(i ->  FMT.parseDateTime(jsonParser2.parse("5344.data.ts").get(i).asText()), i ->  jsonParser2.parse("5344.data.v").get(i).asDouble()));

            nodes3 = IntStream.range(0, jsonParser3.parse("timestamp").size())
                    .boxed()
                    .collect(Collectors.toMap(i ->   FMT.parseDateTime(jsonParser3.parse("timestamp").get(i).asText()), i ->  jsonParser3.parse("value").get(i).asDouble()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }




    @Test
    void checkSizeOfArray() {

        try {

            assertEquals(nodes1.size(),1000);


            assertEquals(nodes2.size(), 383);


            assertEquals(nodes3.size(), 383);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void isBetween() {
        try {
            DateTime start = new DateTime(2023,1,1,1,00, DateTimeZone.forOffsetHours(1));
            DateTime end = new DateTime(2023,01,05,1,00,DateTimeZone.forOffsetHours(1));

            assertTrue(nodes3.keySet().stream().filter(dateTime -> dateTime.isAfter(start) && dateTime.isBefore(end)).count() == 383);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void containsValues() {
        try {

            assertTrue(nodes3.values().contains(1.1833333333333376));
            assertTrue(nodes3.values().contains(1.41013888888889));
            assertTrue(nodes3.values().contains(1.0097222222222224));
            assertTrue(nodes3.values().contains(0.25));
            assertTrue(nodes3.values().contains(1.0733333333333326));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

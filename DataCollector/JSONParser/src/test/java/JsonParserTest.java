import com.fasterxml.jackson.databind.JsonNode;
import org.jevis.jsonparser.JSONParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static javolution.testing.TestContext.assertEquals;
import static javolution.testing.TestContext.assertTrue;

public class JsonParserTest

{




    @Test
    void checkSizeOfArray() {

        try {
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test.json"));
            JSONParser jsonParser = new JSONParser(inputStream);
            List<JsonNode> nodes = jsonParser.parse("value");
            assertEquals(nodes.size(),1000);

            InputStream inputStream2 = new FileInputStream(new File("src/test/resources/test2.json"));
            jsonParser.setInputStream(inputStream2);
            assertEquals(jsonParser.parse("5344.data.v").size(), 383);

            InputStream inputStream3 = new FileInputStream(new File("src/test/resources/test3.json"));
            jsonParser.setInputStream(inputStream3);
            assertEquals(jsonParser.parse("value").size(), 383);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void isBetween() {

        DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
            JSONParser jsonParser = new JSONParser(inputStream);
            List<JsonNode> nodes = jsonParser.parse("timestamp");
            List<DateTime> dateTimes = nodes.stream().map(jsonNode ->FMT.parseDateTime(jsonNode.asText())).collect(Collectors.toList());
            DateTime start = new DateTime(2023,1,1,1,00, DateTimeZone.forOffsetHours(1));
            DateTime end = new DateTime(2023,01,05,1,00,DateTimeZone.forOffsetHours(1));

            assertTrue(dateTimes.stream().filter(dateTime -> dateTime.isAfter(start) && dateTime.isBefore(end)).count() == 383);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void containsValues() {
        try {
            InputStream inputStream = new FileInputStream(new File("src/test/resources/test3.json"));
            JSONParser jsonParser = new JSONParser(inputStream);
            List<JsonNode> nodes = jsonParser.parse("value");

            List<Double> nodeValues =  nodes.stream().map(JsonNode::asDouble).collect(Collectors.toList());
            assertTrue(nodeValues.contains(1.1833333333333376));
            assertTrue(nodeValues.contains(1.41013888888889));
            assertTrue(nodeValues.contains(1.0097222222222224));
            assertTrue(nodeValues.contains(0.25));
            assertTrue(nodeValues.contains(1.0733333333333326));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

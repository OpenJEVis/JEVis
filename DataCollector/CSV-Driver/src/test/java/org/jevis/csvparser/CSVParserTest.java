package org.jevis.csvparser;

import org.jevis.commons.driver.Result;
import org.jevis.commons.driver.inputHandler.GenericConverter;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CSVParserTest {

    @Test
    void valueFromNewerInputStreamReplacesDuplicateTimestamp() {
        CSVParser parser = createParser();
        InputStream olderFile = csv("Timestamp;Value\n2026-10-06 10:00:00;1.0\n");
        InputStream newerFile = csv("Timestamp;Value\n2026-10-06 10:00:00;2.0\n");

        parser.parse(Arrays.asList(olderFile, newerFile), DateTimeZone.UTC);

        List<Result> results = parser.getResult();
        assertEquals(1, results.size());
        assertEquals(2.0d, results.get(0).getValue());
    }

    private CSVParser createParser() {
        DataPoint dataPoint = new DataPoint();
        dataPoint.setMappingIdentifier("Value");
        dataPoint.setTarget("1234:Value");

        CSVParser parser = new CSVParser();
        parser.setConverter(new GenericConverter());
        parser.setCharset(StandardCharsets.UTF_8);
        parser.setDelimiter(";");
        parser.setQuote(null);
        parser.setHeaderLines(0);
        parser.setDpType("ROW");
        parser.setDpIndex(0);
        parser.setDateIndex(0);
        parser.setTimeIndex(-1);
        parser.setDateFormat("yyyy-MM-dd HH:mm:ss");
        parser.setTimeFormat(null);
        parser.setDecimalSeparator(".");
        parser.setDataPoints(Collections.singletonList(dataPoint));
        return parser;
    }

    private InputStream csv(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}

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
        CSVParser parser = createParser("ROW", 0, 0, "yyyy-MM-dd HH:mm:ss");
        InputStream olderFile = csv("Timestamp;Value\n2026-10-06 10:00:00;1.0\n");
        InputStream newerFile = csv("Timestamp;Value\n2026-10-06 10:00:00;2.0\n");

        parser.parse(Arrays.asList(olderFile, newerFile), DateTimeZone.UTC);

        List<Result> results = parser.getResult();
        assertEquals(1, results.size());
        assertEquals(2.0d, results.get(0).getValue());
    }

    @Test
    void rowAlignmentReadsTimeAxisFromTopToBottom() {
        CSVParser parser = createParser("ROW", 0, 1, "dd.MM.yyyy");

        parser.parse(Collections.singletonList(csv(
                "Name;Date;Value\n" +
                        ";02.10.2026;10.0\n" +
                        ";03.10.2026;11.0\n")), DateTimeZone.UTC);

        List<Result> results = parser.getResult();
        assertEquals(2, results.size());
        assertEquals("2026-10-02", results.get(0).getDate().toString("yyyy-MM-dd"));
        assertEquals(10.0d, results.get(0).getValue());
        assertEquals("2026-10-03", results.get(1).getDate().toString("yyyy-MM-dd"));
        assertEquals(11.0d, results.get(1).getValue());
    }

    @Test
    void columnAlignmentReadsTimeAxisFromLeftToRight() {
        CSVParser parser = createParser("COLUMN", 0, 0, "dd.MM.yyyy");

        parser.parse(Collections.singletonList(csv(
                "Name;02.10.2026;03.10.2026\n" +
                        "Value;10.0;11.0\n")), DateTimeZone.UTC);

        List<Result> results = parser.getResult();
        assertEquals(2, results.size());
        assertEquals("2026-10-02", results.get(0).getDate().toString("yyyy-MM-dd"));
        assertEquals(10.0d, results.get(0).getValue());
        assertEquals("2026-10-03", results.get(1).getDate().toString("yyyy-MM-dd"));
        assertEquals(11.0d, results.get(1).getValue());
    }

    private CSVParser createParser(String alignment, int datapointIndex, int dateIndex, String dateFormat) {
        DataPoint dataPoint = new DataPoint();
        dataPoint.setMappingIdentifier("Value");
        dataPoint.setTarget("1234:Value");

        CSVParser parser = new CSVParser();
        parser.setConverter(new GenericConverter());
        parser.setCharset(StandardCharsets.UTF_8);
        parser.setDelimiter(";");
        parser.setQuote(null);
        parser.setHeaderLines(0);
        parser.setDpType(alignment);
        parser.setDpIndex(datapointIndex);
        parser.setDateIndex(dateIndex);
        parser.setTimeIndex(null);
        parser.setDateFormat(dateFormat);
        parser.setTimeFormat(null);
        parser.setDecimalSeparator(".");
        parser.setDataPoints(Collections.singletonList(dataPoint));
        return parser;
    }

    private InputStream csv(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
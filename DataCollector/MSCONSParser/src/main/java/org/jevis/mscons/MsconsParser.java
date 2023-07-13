package org.jevis.mscons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsconsParser {

    private static final Logger logger = LogManager.getLogger(MsconsParser.class);

    private MsconsSample msconsSample;

    private State state = State.None;



    private MsconsPojo msconsPojo = new MsconsPojo();

    private InputStream inputStream;


    public MsconsParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void parse() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            int r;
            StringBuilder stringBuilder = new StringBuilder();
            while ((r = bufferedReader.read()) != -1) {
                char c = (char) r;
                if (c == '\'') {
                    parseSegment(stringBuilder.toString());
                    stringBuilder.setLength(0);
                } else {
                    stringBuilder.append(c);
                }
            }


        } catch (Exception e) {
            logger.error(e);

        }
    }

    private void parseSegment(String segment) {
        parseUnb(segment);
        parseUnh(segment);
        parseBgm(segment);
        parseDtm(segment);
        parseNad(segment);
        parseLoc(segment);
        parseQuantity(segment);
    }


    private void parseUnb(String segment) {

        Pattern pattern = Pattern.compile("UNB\\+(.*?)\\+(.*?):.*?\\+(.*?):.*?\\+(.*?):(.*?)\\+(.*?)\\+.*?\\+(.*?)($|\\+.*)");
        Matcher matcher = pattern.matcher(segment);
        if (matcher.find()) {
            try {

                msconsPojo.getInterchangeHeader().setSyntaxIdentifier(matcher.group(1));

                msconsPojo.getInterchangeHeader().setSender(matcher.group(2));

                msconsPojo.getInterchangeHeader().setRecipient(matcher.group(3));

                msconsPojo.getInterchangeHeader().setDate(matcher.group(4));

                msconsPojo.getInterchangeHeader().setTime(matcher.group(5));
                msconsPojo.getInterchangeHeader().genearteDateTime();

                msconsPojo.getInterchangeHeader().setControlReference(matcher.group(6));

                msconsPojo.getInterchangeHeader().setApplicationReference(matcher.group(7));


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private void parseUnh(String segment) {

        Pattern pattern = Pattern.compile("UNH\\+(.*?)\\+(.*?):(.*?):(.*?):(.*?):(.*?)($|\\+|:).*");
        Matcher matcher = pattern.matcher(segment);
        if (matcher.find()) {
            try {
                msconsPojo.getMessageHeader().setMessageIdentifier(matcher.group(1));

                msconsPojo.getMessageHeader().setMessageType(matcher.group(2));

                msconsPojo.getMessageHeader().setMessageVersion(matcher.group(3));

                msconsPojo.getMessageHeader().setMessageRelease(matcher.group(4));


                msconsPojo.getMessageHeader().setControllingAgency(matcher.group(5));

                msconsPojo.getMessageHeader().setAssociationAssignedCode(matcher.group(6));

                if (!msconsPojo.getMessageHeader().getMessageType().equals("MSCONS")) {
            System.out.println("no MSCONS message");
        }



                } catch (Exception e) {
               logger.error(e);
            }


        }
//
    }

    private void parseQuantity(String segment) {
        Pattern pattern = Pattern.compile("QTY\\+(.*?):(.*?)(:(.*?)$|$)");
        Matcher matcher = pattern.matcher(segment);
        if (matcher.find()) {
            if (matcher.group(1).equals("220")) {
                state = State.Sample;
                msconsSample = new MsconsSample();
                msconsSample.setQuantity(Double.valueOf(matcher.group(2)));
            }
        }
    }

    private void parseBgm(String segment) {

        Pattern pattern = Pattern.compile("BGM\\+(.*?)\\+(.*?)\\+(.*?)($|\\+.*$)");
        Matcher matcher = pattern.matcher(segment);
        if (matcher.find()) {
            try {
                msconsPojo.getMessageHeader().setMessageName(matcher.group(1));

                msconsPojo.getMessageHeader().setMessageIdentification(matcher.group(2));

                msconsPojo.getMessageHeader().setMessageFunction(matcher.group(3));

            } catch (Exception e) {
              logger.error(e);
            }


        }

    }


    private void parseNad(String segment) {
        Pattern pattern = Pattern.compile("NAD\\+(.*?)\\+(.*?):(.*?):(.*?)$");
        Matcher matcher = pattern.matcher(segment);
        if (matcher.find()) {
            if (matcher.group(1).equals("MS")) {
                msconsPojo.getMessageHeader().setSender(matcher.group(2));

            } else if (matcher.group(1).equals("MR")) {
                msconsPojo.getMessageHeader().setRecipient(matcher.group(2));

            } else if (matcher.group(2).equals("DP")) {
                msconsPojo.getMessageHeader().setDeliveryParty(matcher.group(2));
            }
        }

    }

    private void parseLoc(String segment) {
        Pattern pattern1 = Pattern.compile("LOC\\+(.*?)\\+(.*?)$");
        Matcher matcher1 = pattern1.matcher(segment);
        Pattern pattern2 = Pattern.compile("LOC\\+(.*?)\\+(.*?):(.*?):(.*?)$");
        Matcher matcher2 = pattern2.matcher(segment);
        if (matcher2.find()) {
            state = State.Messlokation;
            try {
                msconsPojo.getMesslokation().add(new Messlokation(matcher2.group(2)));
            } catch (Exception e) {
                logger.error(e);
            }

        }else if (matcher1.find()) {
            state = State.Messlokation;
            try {
                msconsPojo.getMesslokation().add(new Messlokation(matcher1.group(2)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parseDtm(String segment) {
        Pattern pattern = Pattern.compile("DTM\\+(.*?):(.*?):(.*?)($|\\+.*|:.*)");
        Matcher matcher = pattern.matcher(segment);
        if (matcher.find()) {
            if (matcher.group(1).equals("163")) {
                if (state == State.Sample) {
                    msconsSample.setStart(convertDateTime(matcher.group(2)));
                } else if (state == State.Messlokation) {
                    msconsPojo.getLastMesslokation().setFromDateTime(convertDateTime(matcher.group(2)));
                }


            } else if (matcher.group(1).equals("164")) {
                if (state == State.Sample) {
                    msconsSample.setEnd(convertDateTime(matcher.group(2)));
                    msconsPojo.getLastMesslokation().getSampleList().add(msconsSample);
                } else if (state == State.Messlokation) {
                    msconsPojo.getLastMesslokation().setUntilDateTime(convertDateTime(matcher.group(2)));
                }

            }


        }
    }

    private DateTime convertDateTime(String string) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmZ");

        DateTime dateTime = dateTimeFormatter.parseDateTime(string.replace("?", ""));
        return dateTime;
    }

    public MsconsPojo getMsconsPojo() {
        return msconsPojo;
    }

    private enum State{
        None,Messlokation, Sample,
    }
}

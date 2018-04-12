/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector;

import java.util.ArrayList;
import java.util.List;
import org.jevis.commons.parsing.DataCollectorParser;
import org.jevis.commons.parsing.GenericParser;
import org.jevis.commons.parsing.ParsingRequest;
import org.jevis.commons.parsing.ParsingRequestGenerator;
import org.jevis.commons.parsing.inputHandler.InputHandler;
import org.jevis.commons.parsing.outputHandler.OutputHandler;
import org.jevis.jedatacollector.connection.DataCollectorConnection;
import org.jevis.jedatacollector.data.Data;
import org.jevis.jedatacollector.data.DataSource;
import org.jevis.jedatacollector.data.DataPoint;
import org.jevis.jedatacollector.data.DataPointDir;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author bf
 */
public class RequestGenerator {

//    public static List<Request> createJEVisRequests(Data data) {
//        List<Request> requests = new ArrayList<Request>();
//
//        DatacollectorConnection connection = data.getConnection();
//        DataCollectorParser parsing = data.getParsing();
//        Equipment equipment = data.getEquipment();
//        List<NewDataPoint> datapoints = data.getDatapoints();
//
//        if (equipment.isSingleConnection()) {
//            for (NewDataPoint dp : datapoints) {
//                Request request = new DefaultRequest();
//                request.setNeedConnection(true);
//                request.setConnection(connection);
//                request.setParser(parsing);
//                request.setEquipment(equipment);
//                request.setSpecificDatapoint(dp);
////                request.setData(data);
//                request.setNeedImport(true);
//                request.setNeedParsing(true);
//                requests.add(request);
//            }
//        } else {
//            Request request = new DefaultRequest();
//            request.setNeedConnection(true);
//            request.setConnection(connection);
//            request.setParser(parsing);
//            request.setEquipment(equipment);
//            request.setSpecificDatapoint(null);
//            request.setNeedImport(true);
//            request.setNeedParsing(true);
//            requests.add(request);
//        }
//
//        return requests;
//    }
    public static Request createOnlyParsingRequest(DataCollectorParser fileParser, List<DataPoint> datapoints, List<InputHandler> inputHandlers) {
        Request req = new DefaultRequest();
        req.setNeedConnection(false);
        req.setNeedImport(false);
        req.setNeedParsing(true);
        req.setParser(null);
        req.setParser(fileParser);
        req.setInputHandlers(inputHandlers);
        req.setDataPoints(datapoints);
        return req;
    }

    public static Request createOnlyParsingRequestWithOutput(GenericParser fileParser, InputHandler input, String outputPath) {
        Request req = new DefaultRequest();
        req.setNeedConnection(false);
        req.setNeedImport(true);
        req.setNeedParsing(true);
        req.setParser(null);
        req.setParser(fileParser);
        req.setInputHandler(input);
        ParsingRequest preq = ParsingRequestGenerator.generateOutputfileRequest(outputPath);
        req.setParsingRequest(preq);
        req.setOutputType(OutputHandler.FILE_OUTPUT);
        req.setFileOutputPath(outputPath);
        return req;
    }

    public static Request createConnectionParsingRequest(DataCollectorConnection connection, DataCollectorParser parsing, List<DataPoint> datapoints) {
        Request request = new DefaultRequest();
        request.setNeedConnection(true);
        request.setConnection(connection);
        request.setParser(parsing);
        request.setDataPoints(datapoints);
//            request.setSpecificDatapoint(dp);
        request.setNeedImport(false);
        request.setNeedParsing(true);
//        request.setOutputType(OutputHandler.JEVIS_OUTPUT);
        String timezone = connection.getTimezone();
//        if (timezone == null) {
//            timezone = "UTC";
//        }
//        ParsingRequest parsingReq = ParsingRequestGenerator.g(DateTimeZone.forID(timezone), Launcher.getClient());
//        request.setParsingRequest(parsingReq);
        return request;
    }

    public static Request createCLIRequest(DataCollectorConnection connection, GenericParser parsing, DataPoint dataPoint, DateTime from, DateTime until, DateTimeZone timeZone) {
        Request request = new DefaultRequest();
        request.setNeedConnection(true);
        request.setConnection(connection);
        request.setFrom(from);
        request.setUntil(until);
        request.setParser(parsing);
        request.setNeedImport(true);
//        request.setSpecificDatapoint(dataPoint);
        List<DataPoint> dataPoints = new ArrayList<DataPoint>();
        dataPoints.add(dataPoint);
        request.setDataPoints(dataPoints);
        request.setNeedParsing(true);
        request.setTimeZone(timeZone);
        return request;
    }

    public static Request createCLIRequestWithFileOutput(DataCollectorConnection connection, GenericParser parsing, DataPoint dataPoint, DateTime from, DateTime until, DateTimeZone timeZone, String outputPath) {
        Request request = new DefaultRequest();
        request.setNeedConnection(true);
        request.setConnection(connection);
        request.setFrom(from);
        request.setUntil(until);
        request.setParser(parsing);
        request.setNeedImport(true);
//        request.setSpecificDatapoint(dataPoint);
        List<DataPoint> dataPoints = new ArrayList<DataPoint>();
        dataPoints.add(dataPoint);
        request.setDataPoints(dataPoints);
        request.setNeedParsing(true);
        request.setTimeZone(timeZone);
        request.setOutputType(OutputHandler.FILE_OUTPUT);
        ParsingRequest preq = ParsingRequestGenerator.generateOutputfileRequest(outputPath);
        request.setParsingRequest(preq);
        return request;
    }

    public static Request createConnectionRequestWithTimeperiod(DataCollectorConnection connection, DataPoint datapoint, DateTime from, DateTime until) {
        Request request = new DefaultRequest();
        request.setNeedConnection(true);
        request.setConnection(connection);
        request.setNeedImport(false);
        request.setNeedParsing(false);
        request.setFrom(from);
        request.setUntil(until);
        ArrayList<DataPoint> datapoints = new ArrayList<DataPoint>();
        datapoints.add(datapoint);
        request.setDataPoints(datapoints);
//        request.setSpecificDatapoint(datapoint);
        return request;
    }

    static Request createJEVisRequest(Data data) {
        DataCollectorConnection connection = data.getConnection();
        DataCollectorParser parsing = data.getParsing();
        DataSource equipment = data.getEquipment();
        List<DataPoint> datapoints = data.getDatapoints();

        Request request = new DefaultRequest();
        request.setNeedConnection(true);
        request.setConnection(connection);
        request.setParser(parsing);
        request.setEquipment(equipment);
        request.setDataPoints(datapoints);
//            request.setSpecificDatapoint(dp);
        request.setData(data);
        request.setNeedImport(true);
        request.setNeedParsing(true);
        request.setTimeZone(equipment.getTimezone());
        request.setOutputType(OutputHandler.JEVIS_OUTPUT);
        ParsingRequest parsingReq = ParsingRequestGenerator.generateJEVisParsingRequest(equipment.getTimezone(), Launcher.getClient());
        request.setParsingRequest(parsingReq);
        return request;
    }

    static Request createJEVisRequest(DataCollectorParser parser, DataCollectorConnection connection, List<DataPoint> datapoints) {

        Request request = new DefaultRequest();
        request.setNeedConnection(true);
        request.setConnection(connection);
        request.setParser(parser);
        request.setDataPoints(datapoints);
//            request.setSpecificDatapoint(dp);
        request.setNeedImport(true);
        request.setNeedParsing(true);
//        request.setOutputType(OutputHandler.JEVIS_OUTPUT);
        String timezone = connection.getTimezone();
        if (timezone == null) {
            timezone = "UTC";
        }
        ParsingRequest parsingReq = ParsingRequestGenerator.generateJEVisParsingRequest(DateTimeZone.forID(timezone), Launcher.getClient());
        request.setParsingRequest(parsingReq);
        return request;
    }

    public static Request createConnectionRequest(DataCollectorConnection connection, List<DataPoint> datapoints) {

        Request request = new DefaultRequest();
        request.setNeedConnection(true);
        request.setConnection(connection);
        request.setDataPoints(datapoints);
        request.setNeedImport(false);
        request.setNeedParsing(false);
        return request;
    }

}

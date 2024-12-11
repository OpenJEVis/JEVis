/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI.
 * <p>
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.emaildatasource;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EMailDataSource implements DataSource {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(EMailDataSource.class);

    private List<JEVisObject> _channels;
    private JEVisObject _dataSource;
    private Importer _importer;
    private Parser _parser;
    private List<Result> _result;
    private EMailServerParameters _eMailServerParameters;
    private EMailConnection _eMailConnection;
    private EMailChannelParameters _channelParameters;

    @Override
    public void run() {

        for (JEVisObject channel : _channels) {
            //mess
            final long timeStart = System.currentTimeMillis();
            //
            try {
                logger.info("Starting with IMAP Channel: [{}] {} ", channel.getID(), channel.getName());
                _result = new ArrayList<>();

                logger.debug("Init parser: {} ", DataCollectorTypes.Parser.NAME);
                JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
                JEVisObject parser = channel.getChildren(parserJevisClass, true).get(0);

                _parser = ParserFactory.getParser(parser);
                logger.debug("parser to string: {}", _parser.toString());
                _parser.initialize(parser);
                logger.debug("done loading parser: {} ", DataCollectorTypes.Parser.NAME);

                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                List<InputStream> input = this.sendSampleRequest(channel);

                logger.info("Answer list size: {}", input.size());

                if (!input.isEmpty()) {
                    _parser.parse(input, _eMailServerParameters.getTimezone());
                    _result = _parser.getResult();
                }

                if (!_result.isEmpty()) {
                    try {
                        JEVisImporterAdapter.importResults(_result, _importer, channel);
                    } catch (NullPointerException np) {
                        logger.error("File is wrong or parse failed");
                    }
                }


            } catch (Exception ex) {
                logger.error("EMail Driver execution can not continue.", ex);
            }
            final long timeEnd = System.currentTimeMillis();
            logger.info("-----------{} execution time is: {} msec -----------", channel.getName(), (timeEnd - timeStart));
        }
    }

    @Override
    public void initialize(JEVisObject mailObject) {
        _dataSource = mailObject;
        initializeAttributes(mailObject);
        initializeChannelObjects(mailObject);

        _importer = ImporterFactory.getImporter(_dataSource);
        if (_importer != null) {
            _importer.initialize(_dataSource);
        }
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) {
        logger.info("Starting sending Sample Request IMAP Channel: [{}] {} ", channel.getID(), channel.getName());
        List<InputStream> answerList = new ArrayList<>();
        final long start = System.currentTimeMillis();
        _eMailConnection = EMailManager.createConnection(_eMailServerParameters);
        final long connectDone = System.currentTimeMillis();
        logger.info("Send sample request. Connection parameters and connection: {}  msec.", (connectDone - start));
        _channelParameters = new EMailChannelParameters(channel, _eMailServerParameters.getProtocol(), _eMailServerParameters.getTimezone());
        final long channelDone = System.currentTimeMillis();
        logger.info("Send sample request. Channel parameters: {} msec.", (channelDone - start));
        answerList = EMailManager.getAnswerList(_channelParameters, _eMailConnection);
        logger.info("Mails in response: {}, closing server connection", answerList.size());

        EMailManager.terminate(_eMailConnection);
        logger.info("done closing server connection");
        final long timeTotalSendreq = System.currentTimeMillis();
        logger.info("Send sample request. Time Total: {} msec. for IMAP Channel: [{}] {}", (timeTotalSendreq - start), channel.getID(), channel.getName());
        return answerList;
    }

    @Override
    public void parse(List<InputStream> input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void importResult() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void initializeAttributes(JEVisObject mailObject) {
        try {
            _eMailServerParameters = new EMailServerParameters(mailObject);
        } catch (Exception ex) {
            logger.error("Server settings are incorrect or missing.", ex);
        }
    }

    private void initializeChannelObjects(JEVisObject mailObject) {
        try {
            JEVisClass channelDirClass = mailObject.getDataSource().getJEVisClass(EMailConstants.EMailChannelDirectory.NAME);
            JEVisObject channelDir = mailObject.getChildren(channelDirClass, false).get(0);
            JEVisClass channelClass = mailObject.getDataSource().getJEVisClass(EMailConstants.EMailChannel.NAME);
            _channels = channelDir.getChildren(channelClass, false);
        } catch (Exception ex) {
            logger.error("Could not initialize channel {}:{}", mailObject.getID(), mailObject.getName(), ex);
        }
    }
}

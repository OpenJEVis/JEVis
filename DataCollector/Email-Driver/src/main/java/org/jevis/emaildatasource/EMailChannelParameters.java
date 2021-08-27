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

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.*;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


/**
 * The EMailChannelParameters class represents the settings required to search a
 * special message in the mailbox.
 *
 * @author Artur Iablokov
 */
public class EMailChannelParameters {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(EMailChannelParameters.class);
    private final String _protocol;
    private final DateTimeZone dateTimeZone;
    private String _sender;
    private String _subject;
    private DateTime _lastReadout;
    private SearchTerm _searchTerm;
    private String _filename;
    private boolean _inbody;

    public EMailChannelParameters(JEVisObject channel, String protocol, DateTimeZone dateTimeZone) {
        _protocol = protocol;
        this.dateTimeZone = dateTimeZone;
        try {
            channel.getDataSource().reloadAttribute(channel);
        } catch (Exception e) {
            logger.error("Could not reload attributes for object {}:{}", channel.getName(), channel.getID(), e);
        }

        setChannelAttribute(channel);
        setSearchTerms();
    }

    /**
     * Set the channel attributes
     *
     * @param channel
     *
     */
    private void setChannelAttribute(JEVisObject channel) {

        JEVisClass channelClass;
        try {
            channelClass = channel.getJEVisClass();
        } catch (JEVisException ex) {
            logger.error("failed to get attributes for the channel.", ex);
        }

        _sender = DBHelper.getAttValue(DBHelper.RetType.STRING, channel, EMailConstants.EMailChannel.SENDER, EMailConstants.Errors.SEND_ERR, EMailConstants.DefParameters.SENDER);
        _subject = DBHelper.getAttValue(DBHelper.RetType.STRING, channel, EMailConstants.EMailChannel.SUBJECT, EMailConstants.Errors.SUBJ_ERR, EMailConstants.DefParameters.SUBJECT);
        _lastReadout = DBHelper.getAttValue(DBHelper.RetType.DATETIME, channel, EMailConstants.EMailChannel.LAST_READOUT, EMailConstants.Errors.LASTR_ERR, EMailConstants.DefParameters.LAST_READ);
        _filename = DBHelper.getAttValue(DBHelper.RetType.STRING, channel, EMailConstants.EMailChannel.FILENAME, EMailConstants.Errors.FILENAME_ERR, EMailConstants.DefParameters.FILENAME);
        _inbody = DBHelper.getAttValue(DBHelper.RetType.BOOLEAN, channel, EMailConstants.EMailChannel.INBODY, EMailConstants.Errors.BODY_ERR, EMailConstants.DefParameters.INBODY);
    }

    /**
     * Set the terms for message search
     *
     *
     *
     */
    private void setSearchTerms() {

        if (_lastReadout != null) {
            if (_protocol.equalsIgnoreCase(EMailConstants.Protocol.IMAP)) {
                _searchTerm = termBuilderIMAP();
            } else if (_protocol.equalsIgnoreCase(EMailConstants.Protocol.POP3)) {
                _searchTerm = termBuilder();
            } else {
                logger.error("Failed to set up the Search Term");
            }

        } else {
            logger.error("Last readout date is not valid. Enter the correct value, or leave the field empty.");
        }

//        if (_lastReadout != null) {
//            SearchTerm newerThan = null;
//            SearchTerm subjectTerm = null;
//            SearchTerm senderTerm = null;
//            SearchTerm tempTerm = null;
//
//            try {
//                newerThan = new ReceivedDateTerm(ComparisonTerm.GT, _lastReadout.toDate());
//            } catch (NullPointerException ex) {
//                Logger.getLogger(EMailChannelParameters.class.getName()).log(Level.SEVERE, "Date term is wrong", ex);
//            }
//
//            if (!_subject.equals("")) {
//                subjectTerm = new SubjectTerm(_subject);
//            }
//
//            if (!_sender.equals("")) {
//                try {
//                    senderTerm = new FromTerm(new InternetAddress(_sender, true));
//                } catch (AddressException ex) {
//                    Logger.getLogger(EMailChannelParameters.class.getName()).log(Level.SEVERE, "Sender email address is not valid.", ex);
//                    senderTerm = null;
//                }
//            }
//            if (_protocol.equalsIgnoreCase(EMailConstants.Protocol.IMAP)) {
//                if (subjectTerm != null && senderTerm != null) {
//                    tempTerm = new AndTerm(senderTerm, subjectTerm);
//                    _searchTerm = new AndTerm(newerThan, tempTerm);
//                } else if (subjectTerm != null) {
//                    tempTerm = subjectTerm;
//                    _searchTerm = new AndTerm(newerThan, tempTerm);
//                } else if (senderTerm != null) {
//                    tempTerm = senderTerm;
//                    _searchTerm = new AndTerm(newerThan, tempTerm);
//                } else {
//                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Channel parameters are not valid.");
//                    _searchTerm = newerThan;
//                }
//            } else if (_protocol.equalsIgnoreCase(EMailConstants.Protocol.POP3)) {
//                _searchTerm = subjectTerm;
//            }
//        }
    }

    /**
     * Get the search term
     *
     * @return EMail protocol
     *
     */
    public String getProtocol() {
        return _protocol;
    }

    /**
     * Get the search term
     *
     * @return DateTime last Readout
     *
     */
    public DateTime getLastReadout() {
        return _lastReadout;
    }

    /**
     * Get the filename
     *
     * @return DateTime last Readout
     *
     */
    public String getFilename() {
        return _filename;
    }

    /**
     * Get the search term
     *
     * @return SearchTerm
     *
     */
    public SearchTerm getSearchTerms() {
        return _searchTerm;
    }

    /**
     * Get the data in body parameter
     *
     * @return boolean
     *
     */
    public boolean getDataInBody() {
        return _inbody;
    }

    /**
     * Set the subject search term
     *
     * @return SearchTerm
     *
     */
    private SearchTerm setSubjectTerm() {
        SearchTerm term = null;
        term = new SubjectTerm(_subject);
        return term;
    }

    /**
     * Set the sender search term
     *
     * @return SearchTerm
     *
     */
    private SearchTerm setSenderTerm() {
        SearchTerm term = null;
        try {
            term = new FromTerm(new InternetAddress(_sender, true));
        } catch (AddressException ex) {
            logger.error("Sender email address is not valid.", ex);
            term = null;
        }
        return term;
    }

    private SearchTerm termBuilder() {
        SearchTerm temp = null;
        SearchTerm from;
        SearchTerm subj;

        //both parameters are specified.
        if (!_sender.equals("") && !_subject.equals("")) {
            from = setSenderTerm();
            subj = setSubjectTerm();
            //EMail address valid
            if (from != null) {
                temp = new AndTerm(from, subj);
            } else {
                temp = subj;
            }
        } else if (!_sender.equals("")) {
            //EMail address not valid
            from = setSenderTerm();
            if (from == null) {
                logger.error("Sender parameter is not valid. Check your EMail Channel");
            }
        } else if (!_subject.equals("")) {
            temp = setSubjectTerm();
        } else {
            logger.error("EMail channel does not contain parameters. Specify the EMail subject and EMail sender");
        }
        return temp;
    }

    private SearchTerm termBuilderIMAP() {
        SearchTerm temp = termBuilder();
        SearchTerm newerThan = null;
        SearchTerm term;
        try {
            newerThan = new ReceivedDateTerm(ComparisonTerm.GT, _lastReadout.minusDays(1).toDate());
        } catch (NullPointerException ex) {
            logger.error("Date term is wrong", ex);
        }

        // subject and sender not null
        if (temp != null) {
            term = new AndTerm(newerThan, temp);
        } else {
            term = newerThan;
        }

        return term;
    }
}
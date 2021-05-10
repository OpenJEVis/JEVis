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

import org.jevis.commons.driver.DataCollectorTypes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.logging.Level;

/**
 * The EMailConstants class is a container for the constants.
 *
 * @author Artur Iablokov
 */
public interface EMailConstants {

    interface EMailChannelDirectory extends DataCollectorTypes.ChannelDirectory {

        String NAME = "EMail Channel Directory";
    }

    interface EMail {

        String NAME = "EMail Server";
        String TIMEZONE = "Timezone";
        String ENABLE = "Enabled";

        String CONNECTION_TIMEOUT = "Connection Timeout";
        String READ_TIMEOUT = "Read Timeout";
        String HOST = "Host";
        String PORT = "Port";

        String PASSWORD = "Password";
        String USER = "User";
        String AUTHENTICATION = "Authentication";
        String SSL = "SSL";
        String FOLDER = "Folder";

        interface POP3EMail extends EMail {

            String NAME = "POP3 EMail Server";
        }

        interface IMAPEMail extends EMail {

            String NAME = "IMAP EMail Server";
        }
    }

    interface EMailChannel extends DataCollectorTypes.Channel {

        String NAME = "EMail Channel";
        String LAST_READOUT = "Last Readout";
        String SUBJECT = "Subject";
        String SENDER = "Sender";
        String INBODY = "Data in body";
        String FILENAME = "Filename";
    }

    interface Protocol {

        String POP3 = "pop3";
        String IMAP = "imap";
    }

    interface Errors {

        MailError HOST_ERR = new MailError("Host", "536350", Level.SEVERE);
        MailError USER_ERR = new MailError("User EMail", "536351", Level.SEVERE);
        MailError PASS_ERR = new MailError("Paasword", "536352", Level.SEVERE);
        MailError FOLD_ERR = new MailError("Folder name", "536353", Level.WARNING);
        MailError AUTH_ERR = new MailError("Authentication", "536354", Level.WARNING);
        MailError READ_ERR = new MailError("Read timeout", "536355", Level.WARNING);
        MailError CONN_ERR = new MailError("Connection timeout", "536356", Level.WARNING);
        MailError SSL_ERR = new MailError("SSL", "536357", Level.SEVERE);
        MailError TIMEZ_ERR = new MailError("Timezone", "536358", Level.WARNING);
        MailError PORT_ERR = new MailError("Port", "536359", Level.WARNING);
        MailError ENAB_ERR = new MailError("Enable", "536360", Level.SEVERE);
        MailError SEND_ERR = new MailError("Sender", "536361", Level.WARNING);
        MailError SUBJ_ERR = new MailError("Subject", "536362", Level.WARNING);
        MailError LASTR_ERR = new MailError("Last readout", "536363", Level.SEVERE);
        MailError FILENAME_ERR = new MailError("File name", "536364", Level.WARNING);
        MailError BODY_ERR = new MailError("Parse body", "536365", Level.SEVERE);
    }

    interface DefParameters {

        // EMail server parameters
        String FOLDER_NAME = "INBOX";
        String USER_EMAIL = "";
        String HOST = "";
        String PASSWORD = "";
        String AUTHENTICATION = "";
        String SSL = "";
        DateTimeZone TIMEZONE = DateTimeZone.UTC;
        int READ_TIMEOUT = 300;
        int CONNECTION_TIMEOUT = 300;
        Boolean ENABLE = false;
        int PORT = -1;
        // EMail Channel parameters
        String SUBJECT = "";
        String SENDER = "";
        DateTime LAST_READ = new DateTime(2010, 1, 1, 0, 0, 0);  //2010-01-01 00:00:00
        Boolean INBODY = false;
        String FILENAME = "";
    }

    interface ValidValues {

        String TIMEFORMAT = "yyyy-MM-dd HH:mm:ss";

        interface CryptProtocols {

            String SSL_TLS = "SSL/TLS";
            String STARTTLS = "STARTTLS";
        }
    }
}

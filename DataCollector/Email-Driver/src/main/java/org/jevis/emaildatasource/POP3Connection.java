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

import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3SSLStore;
import com.sun.mail.pop3.POP3Store;
import org.apache.logging.log4j.LogManager;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 *
 * @author bi
 */
public class POP3Connection implements EMailConnection {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(POP3Connection.class);

    private static final String DEFAULT_FOLDER = "Inbox";
    private Store _store;
    private POP3Folder _folder;
    private POP3SSLStore _sslStore;
    private String _foldName;

    @Override
    public void setConnection(Session session, EMailServerParameters param) {

        if (param.isSsl()) {
            _store = new POP3SSLStore(session, null);
        } else {
            _store = new POP3Store(session, null);
        }

        try {
            _store.connect(param.getHost(), param.getUserEMail(), param.getPassword());
        } catch (MessagingException ex) {
            logger.error("EMail Connection setting failed. Wrong login data or properties.", ex);
        }

    }

    @Override
    public POP3Folder getFolder() {
        try {
            if (!_store.isConnected()) {
                logger.error("Connected not possible");
            }
            _folder = (POP3Folder) _store.getFolder(DEFAULT_FOLDER);
        } catch (MessagingException ex) {
            logger.error("Unable to open the inbox folder", ex);
        }
        return _folder;
    }

    @Override
    public void terminate() {
        try {
            _folder.close(false);
        } catch (MessagingException ex) {
            logger.error("Email-Folder terminate failed", ex);
        }
        try {
            _store.close();
        } catch (MessagingException ex) {
            logger.error("Email-Store terminate failed", ex);
        }
    }

}

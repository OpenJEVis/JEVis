/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.driver;

import org.jevis.api.JEVisObject;

/**
 * @author bf
 */
public interface DataCollectorTypes {

    interface DataSourceDriverDirectory {

        String NAME = "Data Source Driver Directory";
    }

    interface ParserDriverDirectory {

        String NAME = "Parser Driver Directory";
    }

    interface ConverterDriverDirectory {

        String NAME = "Converter Driver Directory";
    }

    interface ImporterDriverDirectory {

        String NAME = "Importer Driver Directory";
    }

    interface ChannelDirectory {

        String NAME = "Channel Directory";

        interface SOAPChannelDirectory extends ChannelDirectory {

            String NAME = "SOAP Channel Directory";
        }

        interface FTPChannelDirectory extends ChannelDirectory {

            String NAME = "FTP Channel Directory";
        }

        interface sFTPChannelDirectory extends ChannelDirectory {

            String NAME = "sFTP Channel Directory";
        }

        interface JEVisChannelDirectory extends ChannelDirectory {
            String NAME = "JEVis Channel Directory";
        }
    }

    interface JEDataCollector {

        String NAME = "JEDataCollector";
        String MAX_NUMBER_THREADS = "Max Number Threads";
        String DATA_SOURCE_TIMEOUT = "Data Source Timeout";
        String ENABLE = "Enable";
    }

    interface Driver {

        String NAME = "Driver";
        String SOURCE_FILE = "Source File";
        String MAIN_CLASS = "Main Class";
        String JEVIS_CLASS = "JEVis Class";
        String ENABLED = "Enabled";

        interface DataSourceDriver extends Driver {

            String NAME = "Data Source Driver";
        }

        interface ParserDriver extends Driver {

            String NAME = "Parser Driver";
        }

        interface ConverterDriver extends Driver {

            String NAME = "Converter Driver";
        }

        interface ImporterDriver extends Driver {

            String NAME = "Importer Driver";
        }
    }

    interface Parser {

        String NAME = "Parser";
        String CHARSET = "Charset";
    }

    interface Converter {

        String NAME = "Converter";
    }

    interface DataSource {

        String NAME = "Data Source";
        String TIMEZONE = "Timezone";
        String DELETE_ON_SUCCESS = "Delete File on successful parsing";
        String ENABLE = "Enabled";
        String OVERWRITE = "Overwrite";
        String MANUAL_TRIGGER = "Manual Trigger";

        interface DataServer extends DataSource {

            String NAME = "Data Server";
            String CONNECTION_TIMEOUT = "Connection Timeout";
            String READ_TIMEOUT = "Read Timeout";
            String HOST = "Host";
            String PORT = "Port";

            interface FTP extends DataServer {

                String NAME = "FTP Server";
                String PASSWORD = "Password";
                String SSL = "SSL";
                String USER = "User";
            }

            interface sFTP extends DataServer {

                String NAME = "sFTP Server";
                String PASSWORD = "Password";
                String SSL = "SSL";
                String USER = "User";
            }

            interface SOAP extends DataServer {

                String NAME = "SOAP Server";
                String PASSWORD = "Password";
                String SSL = "SSL";
                String USER = "User";
            }

            interface OPCUA extends DataServer {

                String PASSWORD = "Password";
                String USER = "User";
                String PROTOCOL = "Protocol";
            }

            interface JEVisServer extends DataServer {
                String NAME = "JEVis Server";
                String USER = "User";
                String PASSWORD = "Password";
                String SSL = "SSL";
            }
        }
    }

    interface Channel {

        String NAME = "Channel";
        String LAST_READOUT = "Last Readout";

        interface FTPChannel extends Channel {

            String NAME = "FTP Channel";
            String PATH = "Path";
        }

        interface sFTPChannel extends Channel {

            String NAME = "sFTP Channel";
            String PATH = "Path";
        }

        interface SOAPChannel extends Channel {

            String NAME = "SOAP Channel";
            String TEMPLATE = "Template";
            String PATH = "Path";
        }

        interface JEVisChannel extends Channel {
            String NAME = "JEVis Channel";
            String SOURCEID = "Source Id";
            String SOURCEATTRIBUTE = "Source Attribute";
            String TARGETID = "Target Id";
        }
    }

    interface Importer {

        String NAME = "Importer";
    }

    interface DataPointDirectory {

        String NAME = "Data Point Directory";
    }

    interface DataPoint {

        String NAME = "Data Point";

        void setDataPointObject(JEVisObject dp);
    }

}

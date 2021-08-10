/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-WS.
 * <p>
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

/**
 * Simple JEWebService URL building constats interfaces
 *
 * @author fs
 * @TODO Add an URL building function
 */
public interface REQUEST {

    String API_PATH_V1 = "JEWebService/v1/";


    interface ATTRIBUTES {
        String PATH = "attributes/";
    }

    interface OBJECTS {

        String PATH = "objects/";

        interface OPTIONS {

            String INCLUDE_RELATIONSHIPS = "rel=";
            String ONLY_ROOT = "root=";
            String INCLUDE_CHILDREN = "includeChildren=";
        }

        interface ATTRIBUTES {

            String PATH = "attributes/";

            interface OPTIONS {

                String INCLUDE_RELATIONSHIPS = "rel=true";
            }

            interface SAMPLES {

                String PATH = "samples/";

                interface OPTIONS {

                    String FROM = "from=";
                    String UNTIL = "until=";
                    String LATEST = "onlyLatest=";
                    String customWorkDay = "cwd=";
                    String aggregationPeriod = "ap=";
                    String manipulationMode = "mm=";
                    String timeZone = "tz=";
                    String LIMIT = "limit";
                }

                interface FILES {

                    String PATH = "files/";

                    interface OPTIONS {

                        String FILENAME = "filename=";
                        String TIMESTAMP = "timestamp=";
                    }
                }

            }

        }
    }

    interface CLASS_ICONS {

        String PATH = "classicons/";
    }

    interface CLASSES {

        String PATH = "classes/";

        interface OPTIONS {

            String INCLUDE_RELATIONSHIPS = "rel=true";
        }

        interface ICON {

            String PATH = "icon/";
        }


    }


    interface RELATIONSHIPS {

        String PATH = "relationships/";

        interface OPTIONS {

            String INCLUDE_RELATIONSHIPS = "rel=true";
            String FROM = "from=";
            String TO = "to=";
            String TYPE = "type=";
        }
    }
//
//    public interface CLASS_RELATIONSHIPS {
//
//        public static String PATH = "classrelationships/";
//
//        public interface OPTIONS {
//
//            public static String INCLUDE_RELATIONSHIPS = "rel=true";
//            public static String FROM = "from=";
//            public static String TO = "to=";
//            public static String TYPE = "type=";
//        }
//
//    }

    interface JEVISUSER {

        String PATH = "user/";

        interface OPTIONS {

            String INCLUDE_RELATIONSHIPS = "rel=true";
        }
    }

}

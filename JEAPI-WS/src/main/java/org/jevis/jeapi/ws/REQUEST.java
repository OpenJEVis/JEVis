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

    public static String API_PATH_V1 = "JEWebService/v1/";

    public interface OBJECTS {

        public static String PATH = "objects/";

        public interface OPTIONS {

            public static String INCLUDE_RELATIONSHIPS = "rel=";
            public static String ONLY_ROOT = "root=";
        }

        public interface ATTRIBUTES {

            public static String PATH = "attributes/";

            public interface OPTIONS {

                public static String INCLUDE_RELATIONSHIPS = "rel=true";
            }

            public interface SAMPLES {

                public static String PATH = "samples/";

                public interface OPTIONS {

                    public static String FROM = "from=";
                    public static String UNTIL = "until=";
                    public static String LASTEST = "onlyLatest=";
                }

                public interface FILES {

                    public static String PATH = "files/";

                    public interface OPTIONS {

                        public static String FILENAME = "filename=";
                        public static String TIMESTAMP = "timestamp=";
                    }
                }

            }

        }
    }

    public interface CLASS_ICONS {

        public static String PATH = "classicons/";
    }

    public interface CLASSES {

        public static String PATH = "classes/";

        public interface OPTIONS {

            public static String INCLUDE_RELATIONSHIPS = "rel=true";
        }

        public interface ICON {

            public static String PATH = "icon/";
        }


    }


    public interface RELATIONSHIPS {

        public static String PATH = "relationships/";

        public interface OPTIONS {

            public static String INCLUDE_RELATIONSHIPS = "rel=true";
            public static String FROM = "from=";
            public static String TO = "to=";
            public static String TYPE = "type=";
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

    public interface JEVISUSER {

        public static String PATH = "user/";

        public interface OPTIONS {

            public static String INCLUDE_RELATIONSHIPS = "rel=true";
        }
    }

}

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
 * Typed constant interface that defines all JEWebService REST URL path segments
 * and query-parameter prefixes used by the JEAPI-WS HTTP client.
 *
 * <p>Constants are organized as nested interfaces that mirror the URL hierarchy:
 * <pre>
 *   JEWebService/v1/
 *     objects/{id}/
 *       attributes/{name}/
 *         samples/
 *           files/
 *     classes/
 *     relationships/
 *     user/
 *     session/login/
 * </pre>
 *
 * <p>Append query-parameter constants with their value directly, e.g.:
 * {@code OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS + "true"}.
 *
 * @author fs
 */
public interface REQUEST {

    /**
     * Base path prefix for all v1 REST endpoints.
     */
    String API_PATH_V1 = "JEWebService/v1/";

    /** Constants for the top-level {@code attributes/} endpoint. */
    interface ATTRIBUTES {
        /** Path segment for the attributes resource. */
        String PATH = "attributes/";
    }

    /** Constants for the {@code objects/} endpoint and its sub-resources. */
    interface OBJECTS {

        /** Path segment for the objects resource. */
        String PATH = "objects/";

        /** Query-parameter constants for the objects endpoint. */
        interface OPTIONS {

            /** Query param to include relationships in the response ({@code rel=}). */
            String INCLUDE_RELATIONSHIPS = "rel=";
            /** Query param to return only root objects ({@code root=}). */
            String ONLY_ROOT = "root=";
            /** Query param to include child objects recursively ({@code includeChildren=}). */
            String INCLUDE_CHILDREN = "includeChildren=";
            /** Query param to include soft-deleted objects ({@code deleted=}). */
            String DELETED = "deleted=";
            /** Query param to restore a soft-deleted object ({@code restore=}). */
            String RESTORE = "restore=";
            /** Query param to permanently delete an object ({@code deleteForever=}). */
            String DELETE_FOREVER = "deleteForever=";
        }

        /** Constants for the {@code objects/{id}/attributes/} sub-resource. */
        interface ATTRIBUTES {

            /** Path segment for the attributes sub-resource. */
            String PATH = "attributes/";

            /** Query-parameter constants for the attributes sub-resource. */
            interface OPTIONS {

                /** Query param to include relationships ({@code rel=true}). */
                String INCLUDE_RELATIONSHIPS = "rel=true";
            }

            /** Constants for the {@code samples/} sub-resource of an attribute. */
            interface SAMPLES {

                /** Path segment for the samples sub-resource. */
                String PATH = "samples/";

                /** Query-parameter constants for sample retrieval and aggregation. */
                interface OPTIONS {

                    /** Start of the time range ({@code from=}, ISO-8601). */
                    String FROM = "from=";
                    /** End of the time range ({@code until=}, ISO-8601). */
                    String UNTIL = "until=";
                    /** Return only the latest sample ({@code onlyLatest=}). */
                    String LATEST = "onlyLatest=";
                    /** Custom work-day shift ({@code cwd=}). */
                    String customWorkDay = "cwd=";
                    /** Aggregation period ({@code ap=}). */
                    String aggregationPeriod = "ap=";
                    /** Manipulation/aggregation mode ({@code mm=}). */
                    String manipulationMode = "mm=";
                    /** Time-zone override for aggregation ({@code tz=}). */
                    String timeZone = "tz=";
                    /** Maximum number of samples to return ({@code limit}). */
                    String LIMIT = "limit";
                }

                /** Constants for file-type sample payloads. */
                interface FILES {

                    /** Path segment for the files sub-resource. */
                    String PATH = "files/";

                    /** Query-parameter constants for file retrieval. */
                    interface OPTIONS {

                        /** Query param for the file name ({@code filename=}). */
                        String FILENAME = "filename=";
                        /** Query param for the sample timestamp ({@code timestamp=}). */
                        String TIMESTAMP = "timestamp=";
                    }
                }

            }

        }
    }

    /** Constants for the {@code classicons/} endpoint. */
    interface CLASS_ICONS {

        /** Path segment for the class-icons resource. */
        String PATH = "classicons/";
    }

    /** Constants for the {@code classes/} endpoint and its sub-resources. */
    interface CLASSES {

        /** Path segment for the classes resource. */
        String PATH = "classes/";

        /** Query-parameter constants for the classes endpoint. */
        interface OPTIONS {

            /** Query param to include relationships ({@code rel=true}). */
            String INCLUDE_RELATIONSHIPS = "rel=true";
        }

        /** Constants for the class-icon sub-resource. */
        interface ICON {

            /** Path segment for the icon sub-resource. */
            String PATH = "icon/";
        }


    }

    /** Constants for the {@code relationships/} endpoint. */
    interface RELATIONSHIPS {

        /** Path segment for the relationships resource. */
        String PATH = "relationships/";

        /** Query-parameter constants for filtering relationships. */
        interface OPTIONS {

            /** Query param to include relationships ({@code rel=true}). */
            String INCLUDE_RELATIONSHIPS = "rel=true";
            /** Query param for the start-object ID ({@code from=}). */
            String FROM = "from=";
            /** Query param for the end-object ID ({@code to=}). */
            String TO = "to=";
            /** Query param for the relationship type ({@code type=}). */
            String TYPE = "type=";
        }
    }

    /** Constants for the {@code user/} endpoint. */
    interface JEVISUSER {

        /** Path segment for the user resource. */
        String PATH = "user/";

        /** Query-parameter constants for the user endpoint. */
        interface OPTIONS {

            /** Query param to include relationships ({@code rel=true}). */
            String INCLUDE_RELATIONSHIPS = "rel=true";
        }

    }

    /** Constants for the session-management endpoint. */
    interface SESSION {

        /** Path segment for the session resource. */
        String PATH = "session/";

        /** Constants for the session-login sub-resource. */
        interface LOGIN {

            /** Path segment for the login sub-resource. */
            String PATH = "login/";

            /** HTTP header constants for the login request. */
            interface HEADER {

                /** Header name for the session token returned by the server. */
                String INCLUDE_RELATIONSHIPS = "token";
            }
        }

    }

}

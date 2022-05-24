package org.jevis.httpdatasource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathFollower {

    public static String FOLLOW_CLASS = "Dynamic Channel Path";
    private static final Logger logger = LogManager.getLogger(PathFollower.class);
    JEVisClass followClass;
    Link link = null;

    private CloseableHttpClient httpClient;
    HttpClientContext context;

    public PathFollower(JEVisObject channel) {
        logger.debug("New PathFollower: {}", channel);
        if (channel == null) {
            logger.warn("Error channel is null, cancel PathFollower init");
        } else {
            if (channel != null && followClass != null) {

                try {
                    channel.getChildren(followClass, false).forEach(jeVisObject -> {
                        logger.debug("Dynamic Channel: {}", jeVisObject);
                        link = new Link(jeVisObject);
                    });

                } catch (Exception e) {
                    logger.error("Error while init PathFollower: {}", e, e);
                }

            }
        }


    }

    public void setConnection(CloseableHttpClient httpClient, HttpClientContext context) {
        this.httpClient = httpClient;
        this.context = context;
    }


    public Document getDocument(String url) {
        HttpGet httpget = new HttpGet(url);
        String html = "";
        try {
            HttpResponse response = httpClient.execute(httpget, context);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    html = EntityUtils.toString(entity);//Get html source code
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching url: {}", url, e);
        }
        Document doc = Jsoup.parse(html);
        return doc;

    }


    public String startFetching(String baseURL, String documentURL) throws IOException {
        Document document = getDocument(documentURL);
        logger.trace("----DOC Start----");
        logger.trace("Document: {}", document);
        logger.trace("----DOC End----");

        return followLinks(document, getLink(), baseURL);

    }

    private String followLinks(Document document, Link link, String baseURL) throws IOException {
        logger.error("---------------------------------------------------");
        logger.error("followLinks in: {}, link: {}", document.title(), link.getLinkObject());

        Pattern pattern = Pattern.compile(link.patternToFind);
        logger.debug("pattern to find: {}", pattern);

        //Document document = new Document();
        List<String> matches = new ArrayList<>();
        document.select("a").forEach(element -> {
            try {
                logger.debug("Link: '{}' Matches: '{}' = {}", element.text(), link.patternToFind, pattern.matcher(element.text()));
                Matcher matcher = pattern.matcher(element.text());
                boolean match = matcher.matches();
                if (match) {
                    String href = element.attr("href");
                    /* abs: is not working, i guess because we don't not connect via JSoup client so we have to do the relative URL ur self*/
                    //String href = element.attr("abs:href");

                    String absoluteURL = "";
                    if (href.startsWith("http")) {
                        absoluteURL = href;
                    } else {
                        absoluteURL = baseURL + "" + href;
                    }
                    logger.debug("Add match url: {}", absoluteURL);
                    absoluteURL = HTTPDataSource.FixURL(absoluteURL);

                    matches.add(absoluteURL);
                }
            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        });

        logger.debug("matches: {} take element: {}", matches.size(), link.takeElement);
        int i = 0;
        for (String s : matches) {
            logger.debug("Match element[{}]: '{}'", i, s);
            i++;
        }


        String newURL = "";
        /* negative numbers are index from end upwards*/
        if (link.getElementToTake() <= -1) {
            logger.debug("Take matching element: {}", (matches.size() + link.getElementToTake()));
            newURL = matches.get(matches.size() + link.getElementToTake());
        } else if (link.getElementToTake() >= 1) {/* Take index*/
            logger.debug("Take matching element: {}", (link.getElementToTake() - 1));
            newURL = matches.get(link.getElementToTake() - 1);
        }

        logger.debug("Use URL: {}", newURL);


        if (link.subLink != null) {
            link.subLink.setUrl(newURL);
            return followLinks(getDocument(newURL), link.subLink, baseURL);
        } else {
            logger.debug("Last link reached return document for import");
            return newURL;
        }

    }


    public boolean isActive() {
        return link != null;
    }

    public Link getLink() {
        return link;
    }

    class Link {

        private String url = "";
        private Link subLink;
        /* TODO: implement*/
        private int takeElement = 1;
        private String patternToFind = "";
        private JEVisObject linkObject;

        public Link(JEVisObject obj) {
            linkObject = obj;
            getLink(obj);
            logger.trace("New Link: {}", obj);

        }

        public String getURL() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPatternToFind() {
            return patternToFind;
        }

        public void setPatternToFind(String patternToFind) {
            this.patternToFind = patternToFind;
        }

        public JEVisObject getLinkObject() {
            return linkObject;
        }

        /**
         * @return -3 if non is selected, -2 if last element, 0 first element, >0 index if element
         */
        public int getElementToTake() {
            return takeElement;
        }

        private void getLink(JEVisObject obj) {
            try {
                logger.debug("getLink for Object: {}", obj);

                try {
                    JEVisAttribute patternAtt = obj.getAttribute("Match");
                    if (patternAtt != null) {
                        if (patternAtt.hasSample()) {
                            patternToFind = patternAtt.getLatestSample().getValueAsString();
                            logger.trace("pattern value: {}", url);
                        } else {
                            logger.trace("No pattern value");
                        }
                    } else {
                        logger.trace("No pattern attribute");
                    }
                } catch (Exception ex) {
                    logger.error("Error while parsing Match Pattern ", ex, ex);
                }
                ;

                try {
                    JEVisAttribute elementAtt = obj.getAttribute("Element");
                    if (elementAtt != null) {
                        if (elementAtt.hasSample()) {
                            if (elementAtt.getLatestSample().getValueAsString().equalsIgnoreCase("first")) {
                                takeElement = 1;
                            } else if (elementAtt.getLatestSample().getValueAsString().equalsIgnoreCase("last")) {
                                takeElement = -1;
                            } else {
                                takeElement = elementAtt.getLatestSample().getValueAsLong().intValue();
                            }

                            logger.error("Element index value: {}", url);
                        } else {
                            logger.error("No path value");
                        }
                    } else {
                        logger.error("No path attribute");
                    }
                } catch (Exception ex) {
                    logger.error("Error while parsing Element Index, using default: {}", takeElement, ex, ex);
                }
                ;


                try {
                    List<JEVisObject> linksToFollow = obj.getChildren(followClass, false);
                    if (linksToFollow.isEmpty()) {
                        logger.error("no follow link");
                    } else {
                        linksToFollow.forEach(linkObject -> {

                            try {
                                //if (linkObject.getChildren(followClass, false).size() >= 1) {
                                subLink = new Link(linkObject);
                                //}
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (Exception ex) {
                    logger.error("Error while finding SubLinks", ex, ex);
                }


            } catch (Exception ex) {
                logger.error(ex, ex);
            }
            logger.debug("Final path url: {}", url);
        }

        @Override
        public String toString() {
            return "Link{" +
                    "url='" + url + '\'' +
                    ", subLink=" + subLink +
                    ", takeElement=" + takeElement +
                    ", patternToFind='" + patternToFind + '\'' +
                    '}';
        }
    }

}

package org.jevis.httpdatasource;

import org.apache.commons.net.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    public static String FOLLOW_CLASS = "Link Follower";
    private static final Logger logger = LogManager.getLogger(PathFollower.class);
    JEVisClass followClass;
    Link link = null;

    public PathFollower(JEVisObject channel) {
        try {
            followClass = channel.getDataSource().getJEVisClass(FOLLOW_CLASS);
            List<JEVisObject> linksToFollow = channel.getChildren(followClass, false);
        } catch (Exception ex) {
            logger.error("'{}' class not found", FOLLOW_CLASS, ex);
        }


        if (channel != null) {

            try {
                channel.getChildren(followClass, false).forEach(jeVisObject -> {
                    link = new Link(jeVisObject);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String followLinks(String serverUrl, String username, String password, Link link) throws IOException {
        String pathURL = serverUrl + link.getURL();
        Document document;
        if (username == null || password == null || username.equals("") || password.equals("")) {
            String login = username + ":" + password;
            String base64login = new String(Base64.encodeBase64(login.getBytes()));
            document = Jsoup.connect(pathURL)
                    .header("Authorization", "Basic " + base64login)
                    .get();
        } else {
            document = Jsoup.connect(pathURL)
                    .get();
        }

        Pattern pattern = Pattern.compile(getLink().url);

        List<String> matches = new ArrayList<>();
        document.select("a").forEach(element -> {
            Matcher matcher = pattern.matcher(element.text());
            boolean match = matcher.matches();
            if (match) {
                String absHref = element.attr("abs:href");
                matches.add(absHref);
            }
        });


        String newURL = "";
        if (link.takeElement <= -1) {
            newURL = matches.get(matches.size() + link.takeElement);
        } else if (link.takeElement >= 1) {/* Take index*/
            newURL = matches.get(link.takeElement - 1);
        }

        if (link.subLink != null) {
            link.subLink.setUrl(newURL);
            return followLinks("", username, password, link.subLink);
        } else {
            return newURL;
        }

    }

    public String followLinks(String serverUrl, String username, String password) throws IOException {
        return followLinks(serverUrl, username, password, getLink());
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
        private int takeElement = -2;

        public Link(JEVisObject obj) {
            getLink(obj);
        }

        public String getURL() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * @return -3 if non is selected, -2 if last element, 0 first element, >0 index if element
         */
        public int getElementToTake() {
            return takeElement;
        }

        private void getLink(JEVisObject obj) {
            try {
                List<JEVisObject> linksToFollow = obj.getChildren(followClass, false);
                if (linksToFollow.isEmpty()) {
                    logger.error("no follow link");
                }
                linksToFollow.forEach(linkObject -> {
                    try {
                        url = linkObject.getAttribute("Path").getLatestSample().getValueAsString();
                        if (linkObject.getChildren(followClass, false).size() >= 1) {
                            subLink = new Link(linkObject);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        }
    }

}

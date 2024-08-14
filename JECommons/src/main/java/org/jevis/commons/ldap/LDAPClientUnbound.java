package org.jevis.commons.ldap;

import com.unboundid.ldap.sdk.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LDAPClientUnbound {

    private final LDAPConnection connection;

    public LDAPClientUnbound(String host, int port, String bindDN, String password) throws LDAPException {
        this.connection = new LDAPConnection(host, port, bindDN, password);
    }

    public static void main(String[] args) {
        try {
            LDAPClientUnbound ldapAuth = new LDAPClientUnbound(Settings.server, 389, Settings.adminUser, Settings.adminPW);
            System.out.println("Server auth dome");

            String username = Settings.jscUser;
            String password = Settings.jscPW;
            String groupName = Settings.jevisAdminGroup;


            SearchResultEntry searchResultEntry = ldapAuth.searchUserByCommonName(username);
            System.out.println("Login User: " + username + "\nPW: " + password + " == " + searchResultEntry);
            if (searchResultEntry != null) {
                System.out.println("User Authenticated");
                List<String> memberOf = ldapAuth.getMemberOFGroups(searchResultEntry);
                // Crosscheck mit JEVis Roles
            }

            /*
            if (searchResultEntry != null && ldapAuth.authenticate(searchResultEntry.getAttributeValue("distinguishedName"), password)) {
                System.out.println("User authenticated successfully.");
                System.out.println(ldapAuth.isUserInGroup2(searchResultEntry.getDN(), groupName));

                if (ldapAuth.isUserInGroup(username, groupName)) {
                    System.out.println("User is in the group.");
                } else {
                    System.out.println("User is not in the group.");
                }
            } else {
                System.out.println("Authentication failed.");
            }
            */
        } catch (LDAPException e) {
            e.printStackTrace();
        }
    }

    public SearchResultEntry searchUserByCommonName(String commonName) {
        try {
            //String baseDN = "ou=users,dc=example,dc=com";
            //userPrincipalName: jsc@ad.jevis.org

            String baseDN = Settings.searchBase;
            SearchRequest searchRequest = new SearchRequest(
                    baseDN,
                    SearchScope.SUB,
                    Filter.createEqualityFilter("cn", commonName)
            );
            SearchResult searchResult = connection.search(searchRequest);
            if (!searchResult.getSearchEntries().isEmpty()) {
                System.out.println("Suche binhaltet antworten");
                for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                    System.out.println("--");
                    System.out.println("---->" + entry.getDN());
                    entry.getAttributes().forEach(attribute -> {
                        System.out.println("==== " + attribute.getName() + " -> " + attribute.getValue());
                        for (String value : attribute.getValues()) {
                            System.out.println("===========" + value);
                        }

                    });
                    System.out.println("----> Gruppen: " + entry.getAttributeValue("memberOf"));
                    for (String s : entry.toLDIF()) {
                        System.out.println("########## " + s);
                    }
                    System.out.println(entry.toLDIFString());
                }
                System.out.println("-------------------------------------------");

                return searchResult.getSearchEntries().get(0);
            } else {
                System.out.println("User not found.");
                return null;
            }
        } catch (LDAPException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getMemberOFGroups(SearchResultEntry userEntry) {
        List<String> groups = new ArrayList<>();
        Attribute memberOf = userEntry.getAttribute("memberOf");
        if (memberOf != null) groups.addAll(Arrays.asList(memberOf.getValues()));
        return groups;
    }


    public boolean authenticate(String username, String password) {
        try {
            BindRequest bindRequest = new SimpleBindRequest(username, password);//"cn=" + username, password);
            BindResult bindResult = connection.bind(bindRequest);
            return bindResult.getResultCode() == ResultCode.SUCCESS;
        } catch (LDAPException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUserInGroup(String username, String groupName) {
        try {
            //String groupDN = "cn=" + groupName + ",ou=groups,dc=example,dc=com";
            String groupDN = Settings.searchBase;
            SearchRequest searchRequest = new SearchRequest(
                    groupDN,
                    SearchScope.BASE,
                    Filter.createEqualityFilter("member", "uid=" + username + ",ou=users,dc=example,dc=com")
            );
            SearchResult searchResult = connection.search(searchRequest);
            searchResult.getSearchEntries().forEach(searchResultEntry -> {
                System.out.println("Gruppe: " + searchResultEntry.getDN());
            });


            return !searchResult.getSearchEntries().isEmpty();
        } catch (LDAPException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUserInGroup2(String username, String groupName) {
        try {

            SearchRequest searchRequest = new SearchRequest(
                    username,
                    SearchScope.BASE,
                    // Filter.createEqualityFilter("memberOf", "CN=JSC1G,OU=JEVis Admin,DC=ad,DC=jevis,DC=org"),
                    Filter.createEqualityFilter("memberOf", "*"),
                    SearchRequest.NO_ATTRIBUTES);
            SearchResult searchResult = connection.search(searchRequest);
            System.out.println("ööö Results: " +
                    searchResult.getEntryCount());
            if (searchResult.getEntryCount() == 1) {
                System.out.println("The user is a member of the group.");
                // The user is a member of the group.
            } else {
                System.out.println("The user is not a member of the group.");
                // The user is not a member of the group.
            }


            return !searchResult.getSearchEntries().isEmpty();
        } catch (LDAPException e) {
            e.printStackTrace();
            return false;
        }
    }
}
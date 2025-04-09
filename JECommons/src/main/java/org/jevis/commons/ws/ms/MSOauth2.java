package org.jevis.commons.ws.ms;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

public class MSOauth2 {


    private static String OAUTHPATH = "/oauth2/v2.0/token";
    private String SCOPE = "https://graph.microsoft.com/.default";
    private String CLIENT_ID = "";
    private String CLIENT_SECRET = "";
    private String TENANT_ID = "";
    private String API_ENDPOINT = "https://login.microsoftonline.com/";
    private String AUTHORITY = API_ENDPOINT + TENANT_ID;

    private String finalEndpoint = "";
    private String username;
    private String password;

    public MSOauth2(String endpoint, String SCOPE, String TENANT_ID, String CLIENT_ID, String CLIENT_SECRET) {
        this.CLIENT_ID = CLIENT_ID;
        this.TENANT_ID = TENANT_ID;
        this.CLIENT_SECRET = CLIENT_SECRET;
        this.SCOPE = SCOPE;
        this.API_ENDPOINT = endpoint;
        this.AUTHORITY = endpoint + TENANT_ID;
        this.finalEndpoint = AUTHORITY + TENANT_ID + OAUTHPATH;
    }

    public MSOauth2(String TENANT_ID, String CLIENT_ID, String CLIENT_SECRET) {
        this.CLIENT_ID = CLIENT_ID;
        this.TENANT_ID = TENANT_ID;
        this.CLIENT_SECRET = CLIENT_SECRET;
        this.AUTHORITY = API_ENDPOINT + TENANT_ID;
        this.finalEndpoint = AUTHORITY + OAUTHPATH;
    }

    public static void main(String[] args) {
        try {
            MSOauth2 msAuth = new MSOauth2(
                    args[0],//tenant
                    args[1],//client
                    args[2]//c_secret
            );
            String accessToken = msAuth.connect(args[3], args[4]);
            String displayName = msAuth.getUserDisplayName(accessToken);
            System.out.println("User Display Name: " + displayName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String connect(String username, String password) throws IOException, ParseException {
        this.username = username;
        this.password = password;
        return getAccessToken();
    }

    public String getAccessToken() throws IOException, ParseException {
        String tokenEndpoint = finalEndpoint;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(tokenEndpoint);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            String body = "grant_type=password&client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&scope=" + SCOPE +
                    "&username=" + username +
                    "&password=" + password;

            request.setEntity(new StringEntity(body));
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(responseBody);
                return json.getString("access_token");
            }
        }
    }

    public String getUserDisplayName(String accessToken) throws IOException, ParseException {
        String graphEndpoint = "https://graph.microsoft.com/v1.0/me";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpUriRequestBase request = new HttpUriRequestBase("GET", URI.create(graphEndpoint));
            request.setHeader("Authorization", "Bearer " + accessToken);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(responseBody);

                json.keys().forEachRemaining(s -> {
                    System.out.println("--");
                    System.out.println(s + ": " + json.get(s));
                });
                return json.get("displayName").toString();
            }
        }
    }
}

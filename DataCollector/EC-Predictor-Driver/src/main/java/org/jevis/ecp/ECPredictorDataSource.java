package org.jevis.ecp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple Driver for the EchoCharge Predictor
 */
public class ECPredictorDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(ECPredictorDataSource.class);
    private static String CHANNEL = "ECP Channel";
    private static String CHANNEL_DIR = "ECP Channel Directory";
    private JEVisDataSource ds;
    private DateTimeZone timeZone;
    private String userName;
    private String password;
    private String serverURL;
    private int port;
    private boolean overwrite;
    private boolean isReady = false;
    private List<ECChannel> channels = new ArrayList<>();

    public static double[] convertStringToDoubleArray(String input) {
        input = input.replaceAll("[\\[\\]]", "");
        String[] parts = input.split(",");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i].trim());
        }

        return result;
    }

    @Override
    public void run() {
        try {

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


            String AUTH_URL = serverURL + port + "/token";
            String PREDICT_URL = serverURL + port + "/predict/gru";

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            OkHttpClient client = builder.build();
            RequestBody authBody = new FormBody.Builder()
                    .add("username", userName)
                    .add("password", password)
                    .build();
            Request authRequest = new Request.Builder()
                    .url(AUTH_URL)
                    .post(authBody)
                    .build();


            try (Response authResponse = client.newCall(authRequest).execute()) {
                if (!authResponse.isSuccessful()) {
                    logger.error("Authentication failed. Code: {}", authResponse.code());
                    return;
                }

                String authJson = authResponse.body().string();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(authJson);
                String token = json.path("access_token").asText();

                if (token == null || token.isEmpty()) {
                    logger.debug("Token not found in response.");
                    return;
                }

                logger.debug("Token: {}", token);

                channels.forEach(ecChannel -> {
                    try {
                        AtomicBoolean hasMoreDate = new AtomicBoolean(true);
                        int runClout = 0;

                        while (hasMoreDate.get()) {
                            runClout++;
                            logger.info("Start EC channel Readout: {} ", ecChannel);

                            ds.reloadAttribute(ecChannel.getTarget().getAttribute("Value"));
                            ds.reloadAttribute(ecChannel.getSource().getAttribute("Value"));

                            JEVisSample lastSampleTarget = ecChannel.getTarget().getAttribute("Value").getLatestSample();
                            JEVisSample lastSampleSource = ecChannel.getSource().getAttribute("Value").getLatestSample();
                            DateTime lastTS;

                            if (lastSampleSource == null) {
                                logger.debug("No Data in Source");
                                hasMoreDate.set(false);
                                return;
                            }

                            if (lastSampleTarget != null) {
                                lastTS = lastSampleTarget.getTimestamp().minus(Weeks.weeks(1));
                            } else {
                                lastTS = ecChannel.getSource().getAttribute("Value").getTimestampOfFirstSample();
                            }

                            if (lastSampleTarget != null && lastSampleSource.getTimestamp().isBefore(lastSampleTarget.getTimestamp())) {
                                logger.debug("No new Data");
                                hasMoreDate.set(false);
                                return;
                            }

                            if (lastTS == null) {
                                hasMoreDate.set(false);
                                return;
                            }


                            List<JEVisSample> sample = ecChannel.getSource().getAttribute("Value").getSamples(
                                    lastTS,
                                    lastTS.plus(Weeks.weeks(1))
                            );
                            logger.debug("Input Samples: {}", sample.size());

                            if (sample.isEmpty()) {
                                logger.debug("No new Samples");
                            } else {
                                File file = exportToTempCsv(sample);
                                file.deleteOnExit();
                                if (!file.exists()) {
                                    logger.error("TMP file not found, skip channel");
                                    return;
                                }
                                logger.debug("tmp file: {} ", file);

                                RequestBody fileBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                        .addFormDataPart("file", file.getName(),
                                                RequestBody.create(file, MediaType.parse("text/csv")))
                                        .build();


                                Request predictRequest = new Request.Builder()
                                        .url(PREDICT_URL)
                                        .addHeader("Authorization", "Bearer " + token)
                                        .post(fileBody)
                                        .build();


                                try (Response predictResponse = client.newCall(predictRequest).execute()) {
                                    int status = predictResponse.code();
                                    logger.debug("Status Code: {}", status);
                                    String response = predictResponse.body().string();
                                    logger.debug("Response Content: {}", response);

                                    DateTime lastTSinRequest = sample.get(sample.size() - 1).getTimestamp();
                                    double[] array = convertStringToDoubleArray(response);
                                    List<JEVisSample> result = new ArrayList<>();

                                    DateTime nextDate = lastTSinRequest.plus(Minutes.minutes(15));
                                    for (double v : array) {
                                        result.add(ecChannel.getTarget().getAttribute("Value").buildSample(nextDate, v, "From Predictor"));
                                        nextDate = nextDate.plus(Minutes.minutes(15));
                                    }
                                    logger.debug("New Samples:");
                                    result.forEach(jeVisSample -> {
                                        logger.debug(jeVisSample);
                                    });

                                    if (!result.isEmpty()) {
                                        ecChannel.getTarget().getAttribute("Value").addSamples(result);
                                        file.delete();
                                    } else {
                                        hasMoreDate.set(false);
                                    }

                                    //end run after 1000 runs. Zombie workaround and the Session token is also
                                    //limit in time
                                    if (runClout >= 1000) hasMoreDate.set(false);

                                }


                            }
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                });


            }

        } catch (Exception ex) {
            logger.error(ex, ex);
        }


    }

    public File exportToTempCsv(List<JEVisSample> samples) throws IOException {
        File tempFile = Files.createTempFile("ECPredictorDataSource_" + UUID.randomUUID(), ".csv").toFile();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            // CSV Header
            writer.write("Datum;Wert (kW)");
            writer.newLine();

            for (JEVisSample sample : samples) {
                try {
                    writer.write(formatter.print(sample.getTimestamp()) + ";" + numberFormat.format(sample.getValueAsDouble()));
                    writer.newLine();
                } catch (Exception ex) {
                    logger.error("Error while creating tmp file: {}", ex, ex);
                }
            }
        }

        return tempFile;
    }

    @Override
    public void initialize(JEVisObject dataSourceJEVis) {
        try {
            ds = dataSourceJEVis.getDataSource();
            initializeAttributes(dataSourceJEVis);
            initializeChannels(dataSourceJEVis);
            isReady = true;
            System.out.println("Settings:\n" + this.toString());
        } catch (Exception ex) {
            logger.error(ex, ex);
        }

    }

    private void initializeAttributes(JEVisObject httpObject) {
        try {
            JEVisSample userNameS = httpObject.getAttribute("User").getLatestSample();
            JEVisSample passwordS = httpObject.getAttribute("Password").getLatestSample();
            JEVisSample serverS = httpObject.getAttribute("Host").getLatestSample();
            JEVisSample portS = httpObject.getAttribute("Port").getLatestSample();
            JEVisSample timeZoneS = httpObject.getAttribute("Timezone").getLatestSample();
            JEVisSample overWriteS = httpObject.getAttribute("Overwrite").getLatestSample();


            timeZone = timeZoneS != null ? DateTimeZone.forID(timeZoneS.getValueAsString()) : DateTimeZone.UTC;
            userName = userNameS != null ? userNameS.getValueAsString() : "";
            password = passwordS != null ? passwordS.getValueAsString() : "";
            serverURL = serverS != null ? serverS.getValueAsString() : "";
            serverURL = serverS != null ? serverS.getValueAsString() : "";
            port = portS != null ? portS.getValueAsLong().intValue() : 443;
            overwrite = overWriteS != null ? overWriteS.getValueAsBoolean() : false;
            isReady = true;
        } catch (Exception ex) {
            logger.fatal(ex, ex);
        }
    }

    private void initializeChannels(JEVisObject httpObject) {
        try {
            JEVisClass channelDirClass = ds.getJEVisClass(CHANNEL_DIR);
            JEVisClass channelClass = ds.getJEVisClass(CHANNEL);
            List<JEVisObject> channelDirs = httpObject.getChildren(channelDirClass, false);

            channelDirs.forEach(channelDir -> {

                try {
                    channelDir.getChildren(channelClass, true).forEach(channelObject -> {
                        try {
                            JEVisAttribute sourceAtt = channelObject.getAttribute("Source");
                            JEVisAttribute targetAtt = channelObject.getAttribute("Target");

                            TargetHelper thSource = new TargetHelper(ds, sourceAtt);
                            TargetHelper thTarget = new TargetHelper(ds, targetAtt);
                            logger.debug("Source: {}", thSource.getObject());
                            logger.debug("Target: {}", thTarget.getObject());
                            if (thSource.targetObjectAccessible() && thTarget.targetObjectAccessible()) {
                                channels.add(new ECChannel(thSource.getObject().get(0), thTarget.getObject().get(0)));
                            } else {
                                logger.error("target not accessible");
                            }
                        } catch (Exception ex) {
                            logger.error("Error while configuring Channel: {}", channelObject.getName(), ex);
                        }
                    });
                } catch (Exception ex) {
                    logger.error(ex, ex);
                }
            });


        } catch (Exception ex) {
            logger.fatal(ex, ex);
        }
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public void parse(List<InputStream> input) {

    }

    @Override
    public void importResult() {

    }

    @Override
    public boolean isReady(JEVisObject object) {
        return isReady;
    }

    @Override
    public DateTimeZone getTimeZone(JEVisObject object) {
        return timeZone;
    }

    @Override
    public DateTime getLastRun(JEVisObject object) {
        return DataSource.super.getLastRun(object);
    }

    @Override
    public Long getCycleTime(JEVisObject object) {
        return DataSource.super.getCycleTime(object);
    }

    @Override
    public void finishCurrentRun(JEVisObject object) {
        DataSource.super.finishCurrentRun(object);
    }

    @Override
    public String toString() {
        return "org.jevis.ecpdatasource.ECPredictorDataSource{" +
                "ds=" + ds +
                ", timeZone=" + timeZone +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", serverURL='" + serverURL + '\'' +
                ", port=" + port +
                ", overwrite=" + overwrite +
                ", isReady=" + isReady +
                ", channels=" + channels +
                '}';
    }
}

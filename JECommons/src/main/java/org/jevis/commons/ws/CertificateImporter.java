package org.jevis.commons.ws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URL;

public class CertificateImporter {

    private static final Logger logger = LogManager.getLogger(CertificateImporter.class);

    public void checkAndImport(URL destinationURL, String keystore, String keystorepw, boolean isUI, boolean autoAccept) throws Exception {
        boolean needSSL = needSSLDownload(destinationURL);
        if (needSSL) {
            try {
                LoadCerts loadCerts = new LoadCerts();
                loadCerts.importCertificate(destinationURL, keystore, keystorepw, isUI, autoAccept);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public boolean needSSLDownload(URL destinationURL) {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) destinationURL.openConnection();
            conn.connect();
            return false;
        } catch (SSLHandshakeException sslEx) {
            logger.warn("SSL Error: {}", sslEx.getMessage());
            return true;
        } catch (IOException e) {
            return false;
        }

    }
}

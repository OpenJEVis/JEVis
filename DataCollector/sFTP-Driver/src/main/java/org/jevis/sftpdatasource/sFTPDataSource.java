/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.sftpdatasource;

import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.jevis.commons.utils.CommonMethods;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class sFTPDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(sFTPDataSource.class);
    private final List<JEVisObject> channels = new ArrayList<>();
    private String serverURL;
    private Integer port;
    private String userName;
    private String password;
    private DateTimeZone timezone;
    private Parser parser;
    private Importer importer;
    private JEVisFile sshKey;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private String logDataSourceID = "sFTP";
    private Boolean deleteOnSuccess = false;
    private Path tmpKeyFile;

    // Extrahiert den konstanten Startpfad (z. B. "/ext/Log") aus dem Regex
    private static String extractRootPath(String regexPattern) {
        int wildcardIndex = regexPattern.indexOf('(');
        if (wildcardIndex == -1) {
            // Kein dynamischer Teil – der gesamte Pfad ist fix
            return regexPattern;
        }
        String fixedPrefix = regexPattern.substring(0, wildcardIndex);
        // Trim auf letzten Slash, um sicherzustellen, dass es ein Pfad ist
        int lastSlash = fixedPrefix.lastIndexOf('/');
        return lastSlash >= 0 ? fixedPrefix.substring(0, lastSlash) : "/";
    }

    public List<String> findMatchingFiles(SftpClient sftp, String regexPattern, DateTime lastReadOut) throws IOException {
        List<String> matchingFiles = new ArrayList<>();
        Pattern pattern = Pattern.compile(regexPattern);

        String rootPath = extractRootPath(regexPattern);
        walkAndMatch(sftp, rootPath, pattern, lastReadOut, matchingFiles);

        return matchingFiles;
    }

    private void walkAndMatch(SftpClient sftp, String currentPath, Pattern pattern, DateTime lastReadOut, List<String> result) throws IOException {
        logger.debug("{}: walkAndMatch: path: {} ,Last-TS: {}", logDataSourceID, currentPath, lastReadOut);
        for (SftpClient.DirEntry entry : sftp.readDir(currentPath)) {
            String name = entry.getFilename();
            logger.debug("{}: traversing file: {}", logDataSourceID, name);
            if (name.equals(".") || name.equals("..")) continue;

            String fullPath = currentPath.endsWith("/") ? currentPath + name : currentPath + "/" + name;

            if (entry.getAttributes().isDirectory()) {
                walkAndMatch(sftp, fullPath, pattern, lastReadOut, result); // rekursiv tiefer gehen
            } else {
                if (pattern.matcher(fullPath).matches() && entry.getAttributes().getModifyTime().toMillis() > lastReadOut.getMillis()) {
                    result.add(fullPath);
                    logger.debug("{}: File matches: {}", logDataSourceID, fullPath);
                } else {
                    logger.debug("{}: File does not match: {}", logDataSourceID, fullPath);
                }
            }
        }
    }

    @Override
    public void parse(List<InputStream> input) {
        //is done by sendSampleRequest(), because of the delete process control

        parser.parse(input, timezone);
        List<Result> result = parser.getResult();
    }

    @Override
    public void run() {
        SshClient client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        client.start();

        try (ClientSession session = client.connect(userName, serverURL, port)
                .verify(Duration.ofSeconds(readTimeout))
                .getSession()) {

            final Iterable<KeyPair> keyPairs;
            if (sshKey != null && sshKey.getBytes() != null) {
                logger.debug("{}: Keyfile loaded: {}, bytes: {}", logDataSourceID, sshKey.getFilename(), sshKey.getBytes().length);
                FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(tmpKeyFile);
                keyPairs = keyPairProvider.loadKeys(session);
                keyPairs.forEach(keyPair -> {
                    logger.debug("Found Private Key, Algorithm: {}, Encoded: {}, Format(): {}"
                            , keyPair.getPrivate().getAlgorithm()
                            , keyPair.getPrivate().getEncoded()
                            , keyPair.getPrivate().getFormat());
                });
                logger.debug("{}: Keyfile loaded: {}", logDataSourceID, tmpKeyFile.getFileName());
                session.setKeyIdentityProvider((sessionContext) -> keyPairs);
            } else {
                session.addPasswordIdentity(password);
                logger.debug("{}: using password: {}", logDataSourceID, password);
            }

            logger.debug("{}: connect, with timeout: {} sec", logDataSourceID, Duration.ofSeconds(connectionTimeout));
            session.auth().verify(Duration.ofSeconds(connectionTimeout));

            try (SftpClient sftp = SftpClientFactory.instance().createSftpClient(session)) {
                logger.debug("{}: connect successful", logDataSourceID);

                for (JEVisObject channel : channels) {
                    try {
                        JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
                        JEVisObject parserObject = channel.getChildren(parserJevisClass, true).get(0);

                        this.parser = ParserFactory.getParser(parserObject);
                        this.parser.initialize(parserObject);
                        List<InputStream> input = new ArrayList<>();
                        try {

                            JEVisClass channelClass = channel.getJEVisClass();
                            JEVisType pathType = channelClass.getType(DataCollectorTypes.Channel.sFTPChannel.PATH);
                            String regexPattern = DatabaseHelper.getObjectAsString(channel, pathType);
                            JEVisType readoutType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.LAST_READOUT);
                            DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType);


                            List<String> matches = findMatchingFiles(sftp, regexPattern, lastReadout);
                            logger.info("{}: {} files matches Pattern, starting download", logDataSourceID, matches.size());

                            /* Fetch Files */
                            for (String path : matches) {
                                try {
                                    logger.debug("{}: Start Download: {}", logDataSourceID, path);
                                    InputStream inputStream = sftp.read(path);
                                    input.add(inputStream);
                                    logger.debug("{}: Finished Download: {}", logDataSourceID, path);
                                } catch (IOException e) {
                                    logger.error("{}: Error while reading path: {}: {}", logDataSourceID, path, e);
                                }
                            }

                            /* Import Files */
                            logger.debug("{}: Start parsing files: {}", logDataSourceID, input.size());

                            if (input.isEmpty()) {
                                logger.warn("{}: Cant get any data from the device", logDataSourceID);
                            }

                            parser.parse(input, timezone);

                            JEVisImporterAdapter.importResults(parser.getResult(), importer, channel);

                            /* Delete File */
                            if (deleteOnSuccess && parser.getReport().errors().isEmpty()) {
                                matches.forEach(file -> {
                                    try {
                                        logger.debug("{}: Delete File: {}", logDataSourceID, file);
                                        sftp.remove(file);
                                    } catch (Exception ex) {
                                        logger.error("{}: Error while deleting file: {}:{}", logDataSourceID, file, ex);
                                    }
                                });
                            }

                            /* Close input Streams */
                            input.forEach(inputStream -> {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    logger.error("{}: Error while closing file: {}", logDataSourceID, e);
                                    throw new RuntimeException(e);
                                }
                            });

                        } catch (JEVisException ex) {
                            logger.error("{}: JEVisException. For channel {}:{}. {}", logDataSourceID, channel.getID(), channel.getName(), ex.getMessage());
                            logger.debug("{}: JEVisException. For channel {}:{}", logDataSourceID, channel.getID(), channel.getName(), ex);
                        } catch (ParseException ex) {
                            logger.error("{}: Parse Exception. For channel {}:{}. {}", logDataSourceID, channel.getID(), channel.getName(), ex.getMessage());
                            logger.debug("{}: Parse Exception. For channel {}:{}", logDataSourceID, channel.getID(), channel.getName(), ex);
                        } catch (Exception ex) {
                            logger.error("{}: Exception. For channel {}:{}", logDataSourceID, channel.getID(), channel.getName(), ex);
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }

            }
        } catch (Exception e) {
            logger.error("{}: error while connection to", logDataSourceID, e);
        } finally {
            client.stop();
            if (Files.exists(tmpKeyFile)) {
                tmpKeyFile.toFile().delete();
            }
        }
    }

    @Override
    public void importResult() {
        //is done by sendSampleRequest(), because of the delete process control
    }

    @Override
    public void initialize(JEVisObject ftpObject) {
        initializeAttributes(ftpObject);
        initializeChannelObjects(ftpObject);

        importer = ImporterFactory.getImporter(ftpObject);
        importer.initialize(ftpObject);

    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws JEVisException {
        List<InputStream> answerList = new ArrayList<InputStream>();


        return answerList;
    }

    private void initializeAttributes(JEVisObject sftpObject) {
        try {
            logDataSourceID = String.format("[%s] %s -", sftpObject.getID(), sftpObject.getName());
            JEVisClass sftpType = sftpObject.getDataSource().getJEVisClass(DataCollectorTypes.DataSource.DataServer.sFTP.NAME);
            JEVisType server = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.HOST);
            JEVisType port = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.PORT);
            JEVisType connectionTimeout = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.CONNECTION_TIMEOUT);
            JEVisType readTimeout = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.READ_TIMEOUT);
            //            JEVisType maxRequest = type.getType("Maxrequestdays");
            JEVisType user = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.USER);
            JEVisType password = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.PASSWORD);
            JEVisType timezoneType = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.TIMEZONE);
            JEVisType sshKeyType = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.SSH_PRIVATE_KEY);
            JEVisType deleteFileOnSuccessType = sftpType.getType(DataCollectorTypes.DataSource.DataServer.DELETE_ON_SUCCESS);

            serverURL = DatabaseHelper.getObjectAsString(sftpObject, server);
            JEVisAttribute portAttr = sftpObject.getAttribute(port);
            if (!portAttr.hasSample()) {
                this.port = 22;
            } else {
                this.port = DatabaseHelper.getObjectAsInteger(sftpObject, port);
            }

            this.connectionTimeout = DatabaseHelper.getObjectAsInteger(sftpObject, connectionTimeout);
            this.readTimeout = DatabaseHelper.getObjectAsInteger(sftpObject, readTimeout);
            JEVisAttribute userAttr = sftpObject.getAttribute(user);
            if (!userAttr.hasSample()) {
                userName = "";
            } else {
                userName = DatabaseHelper.getObjectAsString(sftpObject, user);
            }
            JEVisAttribute passAttr = sftpObject.getAttribute(password);
            if (!passAttr.hasSample()) {
                this.password = "";
            } else {
                this.password = DatabaseHelper.getObjectAsString(sftpObject, password);
            }

            String timezoneString = DatabaseHelper.getObjectAsString(sftpObject, timezoneType);
            if (timezoneString != null) {
                timezone = DateTimeZone.forID(timezoneString);
            } else {
                timezone = DateTimeZone.UTC;
            }

            JEVisAttribute deleteOnSuccessAttr = sftpObject.getAttribute(deleteFileOnSuccessType);
            if (deleteOnSuccessAttr == null || !deleteOnSuccessAttr.hasSample()) {
                deleteOnSuccess = false;
            } else {
                deleteOnSuccess = DatabaseHelper.getObjectAsBoolean(sftpObject, deleteFileOnSuccessType);
            }

            try {
                sshKey = DatabaseHelper.getObjectAsFile(sftpObject, sshKeyType);
                tmpKeyFile = Files.createTempFile(sshKey.getFilename(), sshKey.getFileExtension());
                sshKey.saveToFile(tmpKeyFile.toFile());
                tmpKeyFile.toFile().deleteOnExit();
            } catch (Exception ex) {
                logger.error("{}: Error loading keyfile: {}", logDataSourceID, ex);
            }

        } catch (JEVisException ex) {
            logger.error("{}: ", logDataSourceID, ex);
        }
    }

    private void initializeChannelObjects(JEVisObject ftpObject) {
        try {
            JEVisClass channelClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.sFTPChannel.NAME);

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channels = CommonMethods.getChildrenRecursive(ftpObject, channelClass);

            channels.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                    this.channels.add(channelObject);
                    try {
                        ftpObject.getDataSource().reloadObject(channelObject);
                    } catch (Exception e) {
                        logger.error("{}: Could not reload attributes for object {}:{}", logDataSourceID, channelObject.getName(), channelObject.getID(), e);
                    }
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });

            logger.info("{}: {}:{} has {} channels", logDataSourceID, ftpObject.getName(), ftpObject.getID(), this.channels.size());
        } catch (Exception ex) {
            logger.error("{}: ", logDataSourceID, ex);
        }
    }
}

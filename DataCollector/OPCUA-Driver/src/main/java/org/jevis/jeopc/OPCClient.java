package org.jevis.jeopc;

import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.UaStackClient;
import org.eclipse.milo.opcua.stack.client.UaStackClientConfig;
import org.eclipse.milo.opcua.stack.client.security.ClientCertificateValidator;
import org.eclipse.milo.opcua.stack.client.transport.uasc.ClientSecureChannel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

public class OPCClient {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(OPCClient.class);

    //private final Logger logger = LoggerFactory.getLogger(getClass());
    private String endpointURL = "";//opc.tcp://10.1.2.128:4840
    private EndpointDescription endpointDescription;
    private OpcUaClient client;
    private UaStackClient uaStackClient;
    private UaClient uaclient;
    private IdentityProvider identification = null;


    /**
     * Crete an new client for the endpoint
     *
     * @param url - for example: opc.tcp://10.1.2.128:4840
     */
    public OPCClient(String url) {
        logger.error("Init OPCClient with url: {}", url);
        this.endpointURL = url;

    }

    public void setIdentification(IdentityProvider identification) {
        this.identification = identification;
    }

    /**
     * Connect to the enpoint and init the client.
     *
     * @throws UaException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void connect() throws UaException, ExecutionException, InterruptedException {
        OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(endpointDescription);
        cfg.setApplicationName(LocalizedText.english("JEVis DataCollector"));
        cfg.setProductUri("JEVis");
        if (identification != null) {
            cfg.setIdentityProvider(identification);
            System.out.println("setIdentityProvider: " + identification.toString());
        }

        SecurityPolicy securityPolicy = SecurityPolicy.fromUri(endpointDescription.getSecurityPolicyUri());
        if (securityPolicy == SecurityPolicy.None) {
            System.out.println("Using Security Setting: " + securityPolicy);
        } else {
            try {

                cfg.setCertificateValidator(new ClientCertificateValidator.InsecureValidator());

                /**
                 SecurityAlgorithm signatureAlgorithm = securityPolicy.getAsymmetricEncryptionAlgorithm();
                 ByteString serverCertificate = endpointDescription.getServerCertificate();
                 //KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);


                 KeyGenerator keyGenerator = new KeyGenerator();
                 keyGenerator.create(KeyGenerator.alias, "192.168.178.29", false);
                 System.out.println("New PubKey: " + keyGenerator.getKeyPair().getPublic());
                 System.out.println("New PriKey: " + keyGenerator.getKeyPair().getPrivate());
                 System.out.println("New Cert  : " + keyGenerator.getGeneratedCert().certificate);
                 //SecurityPolicy.Basic128Rsa15;

                 cfg.setKeyPair(keyGenerator.getKeyPair());
                 cfg.setCertificate(keyGenerator.getGeneratedCert().certificate);
                 **/
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // return new AnonymousProvider();
        }

        client = OpcUaClient.create(cfg.build());
        uaStackClient = client.getStackClient();

        //ClientSecureChannel clientSecureChannel = useSecureConnection(uaStackClient.getConfig());


        uaclient = client.connect().get();
        System.out.println("Done Connect");
    }

    /**
     * private KeyStoreLoader createKeyStore() {
     * KeyStoreLoader loader = null;
     * try {
     * File securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security");
     * if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
     * throw new Exception("unable to create security dir: " + securityTempDir);
     * }
     * LoggerFactory.getLogger(getClass()).info("security temp dir: {}", securityTempDir.getAbsolutePath());
     * loader = new KeyStoreLoader().load();
     * loader.load();
     * } catch (Exception e) {
     * logger.error("Could not load keys {}", e);
     * return null;
     * }
     * return loader;
     * }
     **/

    //https://www.codota.com/web/assistant/code/rs/5c656d961095a500014e3287#L554
    private KeyPair createKey(SecurityPolicy securityPolicy, EndpointDescription endpointDescription) throws Exception {
        /**
         File securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security");
         if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
         throw new Exception("unable to create security dir: " + securityTempDir);
         }
         LoggerFactory.getLogger(getClass())
         .info("security temp dir: {}", securityTempDir.getAbsolutePath());

         KeyStoreLoader loader = new KeyStoreLoader();
         return loader.load().getKeyPair();
         **/
        return null;
    }

    /**
     * Close all connection
     */
    public void close() {
        try {
            uaclient.disconnect();
            uaStackClient.disconnect();
            client.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * The the list of endpoints the device supports.
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<EndpointDescription> getEndpoints() throws ExecutionException, InterruptedException {
        logger.error("Discover Endpoint for: {}", endpointURL);
        List<EndpointDescription> endpointDescriptions = DiscoveryClient.getEndpoints(endpointURL).get();
        endpointDescriptions.forEach(endpointDescription -> {
            try {
                logger.debug("---");
                logger.debug("EndpointUrl: {}", endpointDescription.getEndpointUrl());
                logger.debug("SecurityLevel: {}", endpointDescription.getSecurityLevel());
                logger.debug("SecurityPolicyUri: {}", endpointDescription.getSecurityPolicyUri());
                logger.debug("SecurityMode: {}", endpointDescription.getSecurityMode());
                logger.debug("ServerCertificate.length: {}", endpointDescription.getServerCertificate().length());
                logger.debug("ProductUri: {}", endpointDescription.getServer().getProductUri());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return endpointDescriptions;
    }

    /**
     * TODO:
     * - If an enpoint is configred in JEVis take it
     * - .....
     */
    public EndpointDescription autoSelectEndpoint() throws ExecutionException, InterruptedException {

        for (EndpointDescription endpointDescription1 : getEndpoints()) {
            if (endpointDescription1.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri())
                    && endpointDescription1.getEndpointUrl().contains(endpointURL)) {
                logger.error("Found supported endpoint: {}", endpointDescription1);
                return endpointDescription1;
            }
        }

        logger.error("Did not found supported endpoint using first: {}", getEndpoints().get(0));
        return getEndpoints().get(0);

        /**
         Optional<EndpointDescription> found = getEndpoints().stream()
         .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri())) //
         .findFirst();
         if (found.isPresent()) {
         logger.error("Found supported endpoint: {}", found);
         return found.get();
         } else {
         logger.error("Did not found supported endpoint using first: {}", getEndpoints().get(0));
         return getEndpoints().get(0);
         }
         **/
    }

    /**
     * Set the endpoint to use. This defines the security settings.
     *
     * @param endpoint
     */
    public void setEndpoints(EndpointDescription endpoint) {
        endpointDescription = endpoint;
    }

    /**
     * Get the client.
     *
     * @return
     */
    public OpcUaClient getClient() {
        return client;
    }


    /**
     * returns the data list.
     *
     * @param result
     * @return
     */
    public List<DataValue> getDateValues(HistoryReadResult result) {
        logger.error("result.getStatusCode(): {}", result.getStatusCode());
        HistoryData historyData = (HistoryData) result.getHistoryData().decode(
                client.getSerializationContext()
        );

        if (result.getStatusCode().isGood()) {
            logger.error("Data are good");
            if (historyData.getDataValues() != null) {
                return Lists.newArrayList(historyData.getDataValues());
            } else {
                return new ArrayList<>();
            }
        } else {
            logger.error("Data are bad");
            return new ArrayList<>();
        }


    }

    public HistoryReadResult getHistory(OPCUAChannel channel, org.joda.time.DateTime from, org.joda.time.DateTime until) throws ExecutionException, InterruptedException {
        System.out.println("getHistory2");
        List<HistoryReadValueId> ids = new ArrayList<>();
        //double interval = 900000;



        DateTime fromTS = new DateTime(from.toDate());
        DateTime untilTS = new DateTime(until.toDate());
        System.out.println("From : "+from+"  until: "+until);

        HistoryReadDetails historyReadDetails;

        /** Use Aggregated request if function node is not empty **/
        if(channel.getFunctionNode()!=null && !channel.getFunctionNode().isEmpty()){
            AggregateConfiguration aggregateConfiguration = new AggregateConfiguration(
                    true,
                    false,
                    UByte.valueOf(0),
                    UByte.valueOf(0),
                    false);

            NodeId[] nodes = new NodeId[1];
            NodeId readDetailNodeID = NodeId.parse(channel.getFunctionNode());//
            nodes[0]=readDetailNodeID;
            System.out.println("Aggregation Node: "+readDetailNodeID);

            historyReadDetails  = new ReadProcessedDetails(
                    fromTS,
                    untilTS,
                    channel.getFunctionInterval(),
                    nodes,
                    aggregateConfiguration);

        }else{
            /** use raw data request **/
            historyReadDetails = new ReadRawModifiedDetails(false, fromTS, untilTS, uint(0), true);
        }

        HistoryReadValueId id = new HistoryReadValueId(channel.getOPCNodeId(), null, null, null);
        ids.add(id);
        System.out.println("TargetNode: "+id);

        CompletableFuture<HistoryReadResponse> historyRead = client.historyRead(
                historyReadDetails,
                TimestampsToReturn.Source, false, ids);

        HistoryReadResponse response = historyRead.get();

        logger.debug("Data Size:  {}", response.getResults().length);

        if (response.getResults().length > 0) {
            return response.getResults()[0];
        } else return null;
    }

    /**
     * Browse the tree like opc structure.
     *
     * @return
     */
    public HashMap<String, ReferenceDescription> browse() {
        HashMap<String, ReferenceDescription> map = new HashMap<>();

        browseTree(map, "", Identifiers.RootFolder);

        return map;
    }

    /**
     *
     * @param hashMap
     * @param xpathParent
     * @param browseRoot
     */
    private void browseTree(HashMap<String, ReferenceDescription> hashMap, String xpathParent, NodeId browseRoot) {
        BrowseDescription browse = new BrowseDescription(
                browseRoot,
                BrowseDirection.Forward,
                Identifiers.References,
                true,
                uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
                uint(BrowseResultMask.All.getValue())
        );

        try {
            BrowseResult browseResult = client.browse(browse).get();

            List<ReferenceDescription> references = toList(browseResult.getReferences());

            for (ReferenceDescription rd : references) {
                logger.debug("rd: " + rd);
                String xpath = xpathParent + "/" + rd.getBrowseName().getName();
                NodeId nodeId = new NodeId(rd.getNodeId().getNamespaceIndex(), (UInteger) rd.getNodeId().getIdentifier());
                logger.debug("Add to Map: {}-{}", xpath, rd);
                hashMap.put(xpath, rd);
                browseTree(hashMap, xpath, nodeId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param list of the OPC-UA Nodes
     * @param rootFolder of OPC-UA
     */
    public void browse(ObservableList<PathReferenceDescription> list, String rootFolder) {
        NodeId nodeId = Identifiers.RootFolder;

        PathReferenceDescription pathReferenceDescription = null;

        for (int i = 0; i < rootFolder.split("/").length; i++) {
           List<ReferenceDescription> referenceDescriptionList = browseToRoot (nodeId);

            for (int j = 0; j < referenceDescriptionList.size(); j++) {

                if (referenceDescriptionList.get(j).getBrowseName().getName().equals(rootFolder.split("/")[i])) {
                    nodeId = new NodeId(referenceDescriptionList.get(j).getNodeId().getNamespaceIndex(), (UInteger) referenceDescriptionList.get(j).getNodeId().getIdentifier());
                }
                if (referenceDescriptionList.get(j).getBrowseName().getName().equals(rootFolder.split("/")[rootFolder.split("/").length-1])) {
                    pathReferenceDescription = new PathReferenceDescription(referenceDescriptionList.get(j), "", null);
                    list.add(pathReferenceDescription);

                }
            }
        }


        System.out.println(nodeId);
        browseTree(list,"/"+rootFolder.split("/")[rootFolder.split("/").length-1], nodeId);

    }

    /**
     * browse to specified Root folder
     * @param opcRoot
     * @return
     */
    public List<ReferenceDescription> browseToRoot(NodeId opcRoot) {
        BrowseDescription browse = new BrowseDescription(
                opcRoot,
                BrowseDirection.Forward,
                Identifiers.References,
                true,
                uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
                uint(BrowseResultMask.All.getValue())
        );

        try {
            BrowseResult browseResult = client.browse(browse).get();

            List<ReferenceDescription> references = toList(browseResult.getReferences());

            return references;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private void browseTree(ObservableList<PathReferenceDescription> list, String xpathParent, NodeId browseRoot) {
        BrowseDescription browse = new BrowseDescription(
                browseRoot,
                BrowseDirection.Forward,
                Identifiers.References,
                true,
                uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
                uint(BrowseResultMask.All.getValue())
        );

        try {
            BrowseResult browseResult = client.browse(browse).get();

            List<ReferenceDescription> references = toList(browseResult.getReferences());






            for (ReferenceDescription rd : references) {
                String xpath = xpathParent;
                NodeId nodeId = new NodeId(rd.getNodeId().getNamespaceIndex(), (UInteger) rd.getNodeId().getIdentifier());
                PathReferenceDescription pathReferenceDescription;


                    if (rd.getNodeClass().getValue() == 2) {
                        DataValue datavalue = readValue(nodeId);
                        pathReferenceDescription = new PathReferenceDescription(rd, xpath, datavalue);
                    }else {
                        pathReferenceDescription = new PathReferenceDescription(rd, xpath, null);
                    }

                logger.debug("Add to Map: {}-{}", xpath, rd);
                if (rd.getNodeClass().getValue() == 1 || (rd.getNodeClass().getValue() == 2 )) {
                    Platform.runLater(() -> {
                        System.out.println(pathReferenceDescription.getPath());
                        list.add(pathReferenceDescription);
                    });
                }


                browseTree(list, xpath + "/" + rd.getBrowseName().getName(), nodeId);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("InterruptedException: {}", e);
            e.printStackTrace();
        } catch (ExecutionException e) {
            logger.error("ExecutionException: {}", e);
            e.printStackTrace();
        }
        catch (UaException e) {
            e.printStackTrace();
        }
    }

    /**
     * Not wotking test using a secure connection
     *
     * @param config
     * @return
     * @throws UaException
     */
    @Deprecated
    private ClientSecureChannel useSecureConnection(UaStackClientConfig config) throws UaException {
        SecurityPolicy securityPolicy = SecurityPolicy.fromUri(endpointDescription.getSecurityPolicyUri());

        ClientSecureChannel secureChannel;
        if (securityPolicy == SecurityPolicy.None) {
            secureChannel = new ClientSecureChannel(
                    securityPolicy,
                    endpointDescription.getSecurityMode()
            );
            return null;
        } else {
            KeyPair keyPair = config.getKeyPair().orElseThrow(() ->
                    new UaException(
                            StatusCodes.Bad_ConfigurationError,
                            "no KeyPair configured")
            );
            X509Certificate certificate = config.getCertificate().orElseThrow(() ->
                    new UaException(
                            StatusCodes.Bad_ConfigurationError,
                            "no certificate configured")
            );
            List<X509Certificate> certificateChain = Arrays.asList(
                    config.getCertificateChain().orElseThrow(() ->
                            new UaException(
                                    StatusCodes.Bad_ConfigurationError,
                                    "no certificate chain configured"))
            );
            X509Certificate remoteCertificate = CertificateUtil
                    .decodeCertificate(endpointDescription.getServerCertificate().bytes());
            List<X509Certificate> remoteCertificateChain = CertificateUtil
                    .decodeCertificates(endpointDescription.getServerCertificate().bytes());
            return new ClientSecureChannel(
                    keyPair,
                    certificate,
                    certificateChain,
                    remoteCertificate,
                    remoteCertificateChain,
                    securityPolicy,
                    endpointDescription.getSecurityMode()
            );
        }
    }

    @Deprecated
    private ClientSecureChannel useSecureConnection2(UaStackClientConfig config) throws UaException {
        /**
         cfg.setCertificateValidator(new ClientCertificateValidator() {
        @Override public void validateCertificateChain(List<X509Certificate> list, String s, String... strings) throws UaException {
        System.out.println("Validate: s" + s);
        list.forEach(x509Certificate -> {
        System.out.println("x509Certificate: " + x509Certificate);

        });
        for (String string : strings) {
        System.out.println("String: " + string);
        }
        }

        @Override public void validateCertificateChain(List<X509Certificate> list) throws UaException {
        list.forEach(x509Certificate -> {
        System.out.println("validateCertificateChain: " + x509Certificate);
        });
        }


        });
         **/
        return null;
    }

    public static String printEP(EndpointDescription endpointDescription) {

        System.out.println("---------Endpoint----------");
        System.out.println(endpointDescription.getEndpointUrl());

        System.out.println("UserIdentityTokens:");
        for (UserTokenPolicy userIdentityToken : endpointDescription.getUserIdentityTokens()) {
            System.out.println(" -" + userIdentityToken.toString());
        }

        System.out.println("SecurityMode: " + endpointDescription.getSecurityMode().toString());

        System.out.println("SecurityPolicyUri: " + endpointDescription.getSecurityPolicyUri());
        System.out.println("SecurityLevel: " + endpointDescription.getSecurityLevel());
        System.out.println("TypeId: " + endpointDescription.getTypeId());
        System.out.println("Server: " + endpointDescription.getServer().toString());
        return "";
    }

    /**
     *
     * @param nodeId of Node
     * @return Vale as DataValue
     * @throws UaException
     */
    public  DataValue readValue(NodeId nodeId) throws UaException {

            UaVariableNode node = client.getAddressSpace().getVariableNode(nodeId);
            DataValue value = node.readValue();
        if (value.getStatusCode().isGood()) {
            logger.info("Data is good");
            logger.debug(nodeId+ ":"+value);
            return value;
        } else {
            logger.info("Data is Bad");
            return null;
        }


        }

    /**
     *
     * @param dataValue
     * @param nodeId
     * @throws UaException
     */
    public void writeValue(DataValue dataValue, NodeId nodeId) throws UaException {

            UaVariableNode node = client.getAddressSpace().getVariableNode(nodeId);
            node.writeValue(dataValue);

    }

    /**
     *
     * @param value to be written into Node (Double)
     * @param nodeId
     * @throws UaException
     */
    public void writeValue(Double value, NodeId nodeId) throws UaException {
        logger.info("Value :", value);
        DataValue dataValue = new DataValue(new Variant(value), null, null, null);
        writeValue(dataValue, nodeId);
    }

    /**
     *
     * @param value to be written into Node (Int)
     * @param nodeId
     * @throws UaException
     */
    public void writeValue(Integer value, NodeId nodeId) throws UaException {
        logger.info("Value :", value);
        DataValue dataValue = new DataValue(new Variant(value), null, null, null);
        writeValue(dataValue, nodeId);
    }

    /**
     *
     * @param value to be written int Node (String)
     * @param nodeId
     * @throws UaException
     */
    public void writeValue(String value, NodeId nodeId) throws UaException {
        logger.info("Value :", value);
        DataValue dataValue = new DataValue(new Variant(value), null, null, null);
        writeValue(dataValue, nodeId);
    }

    /**
     *
     * @param value to be written int Node (Bool)
     * @param nodeId
     * @throws UaException
     */
    public void writeValue(Boolean value, NodeId nodeId) throws UaException {
        DataValue dataValue = new DataValue(new Variant(value), null, null, null);
        writeValue(dataValue, nodeId);
    }


    /**
     *
     * @param nodeId
     * @return Datatype of OPC Node
     * @throws UaException
     */
    public String getDataType(NodeId nodeId) throws UaException {

            UaVariableNode node = client.getAddressSpace().getVariableNode(nodeId);
            DataValue value = node.readValue();
            return value.getValue().getValue().getClass().getName();

    }


}


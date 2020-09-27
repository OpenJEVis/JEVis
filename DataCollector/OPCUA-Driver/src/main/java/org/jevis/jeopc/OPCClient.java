package org.jevis.jeopc;

import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.UaStackClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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


    /**
     * Crete an new client for the endpoint
     *
     * @param url - for example: opc.tcp://10.1.2.128:4840
     */
    public OPCClient(String url) {
        logger.error("Init OPCClient with url: {}", url);
        this.endpointURL = url;

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

        client = OpcUaClient.create(cfg.build());
        uaStackClient = client.getStackClient();
        uaclient = client.connect().get();
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
                logger.debug("SecurityLevel: {}", endpointDescription.getSecurityLevel());
                logger.debug("SecurityPolicyUri: {}", endpointDescription.getSecurityPolicyUri());
                logger.debug("SecurityMode: {}", endpointDescription.getSecurityMode());
                logger.debug("ServerCertificate: {}", endpointDescription.getServerCertificate());

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
        return getEndpoints().get(0);
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

    /**
     * Read the historic data from the server.
     *
     * @param nodeId
     * @param from
     * @param until
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public HistoryReadResult getHistory(NodeId nodeId, org.joda.time.DateTime from, org.joda.time.DateTime until) throws ExecutionException, InterruptedException {
        List<HistoryReadValueId> ids = new ArrayList<>();

        DateTime fromTS = new DateTime(from.toDate());
        DateTime untilTS = new DateTime(until.toDate());

        /**
         * IndexRange: is the List of index ranges. Can be used to identify the whole array, a single element of a structure or an array, or a single range of indexes for arrays.
         * Does not seem to work with loytec device
         */
        HistoryReadValueId id = new HistoryReadValueId(nodeId, null, null, null);
        ids.add(id);

        CompletableFuture<HistoryReadResponse> historyRead = client.historyRead(
                new ReadRawModifiedDetails(false, fromTS, untilTS, uint(0), true),
                TimestampsToReturn.Both, false, ids);

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
                logger.error("rd: " + rd);
                String xpath = xpathParent + "/" + rd.getBrowseName().getName();
                NodeId nodeId = new NodeId(rd.getNodeId().getNamespaceIndex(), (UInteger) rd.getNodeId().getIdentifier());
                logger.error("Add to Map: {}-{}", xpath, rd);
                hashMap.put(xpath, rd);
                browseTree(hashMap, xpath, nodeId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void browse(ObservableList<PathReferenceDescription> list) {
        browseTree(list, "", Identifiers.RootFolder);

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
                logger.error("Add to Map: {}-{}", xpath, rd);
                PathReferenceDescription pathReferenceDescription = new PathReferenceDescription(rd, xpath);

                Platform.runLater(() -> {
                    list.add(pathReferenceDescription);
                });

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
    }


}


/**
 * ClientSecureChannel secureChannel;
 * if (securityPolicy == SecurityPolicy.None) {
 * secureChannel = new ClientSecureChannel(
 * securityPolicy,
 * endpointDescription.getSecurityMode()
 * );
 * } else {
 * KeyPair keyPair = config.getKeyPair().orElseThrow(() ->
 * new UaException(
 * StatusCodes.Bad_ConfigurationError,
 * "no KeyPair configured")
 * );
 * <p>
 * X509Certificate certificate = config.getCertificate().orElseThrow(() ->
 * new UaException(
 * StatusCodes.Bad_ConfigurationError,
 * "no certificate configured")
 * );
 * <p>
 * List<X509Certificate> certificateChain = Arrays.asList(
 * config.getCertificateChain().orElseThrow(() ->
 * new UaException(
 * StatusCodes.Bad_ConfigurationError,
 * "no certificate chain configured"))
 * );
 * <p>
 * X509Certificate remoteCertificate = CertificateUtil
 * .decodeCertificate(endpoint.getServerCertificate().bytes());
 * <p>
 * List<X509Certificate> remoteCertificateChain = CertificateUtil
 * .decodeCertificates(endpoint.getServerCertificate().bytes());
 * <p>
 * secureChannel = new ClientSecureChannel(
 * keyPair,
 * certificate,
 * certificateChain,
 * remoteCertificate,
 * remoteCertificateChain,
 * securityPolicy,
 * endpoint.getSecurityMode()
 * );
 * }
 **/

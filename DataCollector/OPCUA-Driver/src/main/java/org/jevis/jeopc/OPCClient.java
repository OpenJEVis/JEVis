package org.jevis.jeopc;

import com.google.common.collect.Lists;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.ServerStatusTypeNode;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.UaStackClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;

import java.util.ArrayList;
import java.util.Arrays;
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



    public OPCClient(String endpoint) {
        logger.error("Init OPCClient with url: {}",endpoint);
        this.endpointURL = endpoint;

    }
    public void connect() throws UaException, ExecutionException, InterruptedException {
        OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(endpointDescription);

        client = OpcUaClient.create(cfg.build());
        uaStackClient = client.getStackClient();
        uaclient = client.connect().get();
    }
    public void close(){
        try{
            uaclient.disconnect();
            uaStackClient.disconnect();
            client.disconnect();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    public List<EndpointDescription> getEndpoints() throws ExecutionException, InterruptedException {
        logger.error("Discover Endpoint for: {}",endpointURL);
        List<EndpointDescription> endpointDescriptions = DiscoveryClient.getEndpoints(endpointURL).get();
        endpointDescriptions.forEach(endpointDescription -> {
            try {
                System.out.println(endpointDescription);
                System.out.println("getSecurityLevel:\t\t" + endpointDescription.getSecurityLevel());
                System.out.println("getSecurityPolicyUri:\t" + endpointDescription.getSecurityPolicyUri());
                System.out.println("getSecurityMode:\t" + endpointDescription.getSecurityMode());
                System.out.println("getServerCertificate:\t" + endpointDescription.getServerCertificate());

                //SecurityPolicy securityPolicy = SecurityPolicy.fromUri(endpointDescription.getSecurityPolicyUri());


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

    public void setEndpoints(EndpointDescription endpoint) {
        endpointDescription = endpoint;
    }

    public OpcUaClient getClient() {
        return client;
    }



    public NodeId toNodeID(ReferenceDescription referenceDescription) {
        NodeId nodeId = new NodeId(referenceDescription.getNodeId().getNamespaceIndex(), (UInteger) referenceDescription.getNodeId().getIdentifier());
        System.out.println("r1: " + read(client, referenceDescription.getReferenceTypeId()));

        return nodeId;

    }

    public List<DataValue> getDateValues(HistoryReadResult result){
        HistoryData historyData = (HistoryData) result.getHistoryData().decode(
                client.getSerializationContext()
        );

        return Lists.newArrayList(historyData.getDataValues());
    }

    public HistoryReadResult getHistory(NodeId nodeId, org.joda.time.DateTime from, org.joda.time.DateTime until) throws ExecutionException, InterruptedException {
        List<HistoryReadValueId> ids = new ArrayList<>();
        List<DataValue> dataValues = new ArrayList<>();

        DateTime fromTS = new DateTime(from.toDate());
        DateTime untilTS = new DateTime(until.toDate());

        HistoryReadValueId id = new HistoryReadValueId(nodeId, 20100 + "", null, null);

        ids.add(id);

        CompletableFuture<HistoryReadResponse> historyRead = client.historyRead(
                new ReadRawModifiedDetails(false, fromTS, untilTS, uint(1000), true),
                TimestampsToReturn.Server, false, ids);

        HistoryReadResponse response = historyRead.get();

        System.out.println("Size: " + response.getResults().length);


        if (response.getResults().length > 0) {
            return response.getResults()[0];
        } else return null;


        /**
         for (HistoryReadResult result : response.getResults()) {
         System.out.println("Result: "+result.toString());
         HistoryData historyData = (HistoryData) result.getHistoryData().decode(
         client.getSerializationContext()
         );
         for (DataValue dataValue : historyData.getDataValues()) {
         logger.error("ReadValue: Value={} seTime={} soTime={}", dataValue.getValue(), dataValue.getServerTime(), dataValue.getSourceTime());
         }

         }
         **/
    }

    public HashMap<String,ReferenceDescription> browse(){
        HashMap<String,ReferenceDescription> map = new HashMap<>();

        browseTree(map,"",Identifiers.RootFolder);

        return map;
    }

    private void browseTree(HashMap<String,ReferenceDescription> hashMap,String xpathParent,NodeId browseRoot){
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
                logger.error("rd: "+rd);
                String xpath=xpathParent+"/"+rd.getBrowseName().getName();
                NodeId nodeId = new NodeId(rd.getNodeId().getNamespaceIndex(), (UInteger) rd.getNodeId().getIdentifier());
                logger.error("Add to Map: {}-{}",xpath,rd);
                hashMap.put(xpath,rd);
                browseTree(hashMap,xpath,nodeId);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void browse(ObservableList<PathReferenceDescription> list){
        browseTree(list,"",Identifiers.RootFolder);

    }

    private void browseTree(ObservableList<PathReferenceDescription> list, String xpathParent, NodeId browseRoot){
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
                String xpath=xpathParent;
                NodeId nodeId = new NodeId(rd.getNodeId().getNamespaceIndex(), (UInteger) rd.getNodeId().getIdentifier());
                logger.error("Add to Map: {}-{}",xpath,rd);
                PathReferenceDescription pathReferenceDescription = new PathReferenceDescription(rd,xpath);

                list.add(pathReferenceDescription);
                browseTree(list,xpath+"/"+rd.getBrowseName().getName(),nodeId);
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("InterruptedException: {}",e);
            e.printStackTrace();
        } catch (ExecutionException e) {
            logger.error("ExecutionException: {}",e);
            e.printStackTrace();
        }
    }


    /**
     * ---------------------- OLd ---------------------------------------
     **/


    public void test(OpcUaClient client) {
        try {

            System.out.println("--------------------------- status ------------------");
            ServerTypeNode serverNode = client.getAddressSpace().getObjectNode(
                    Identifiers.Server,
                    ServerTypeNode.class
            ).get();

            String[] serverArray = serverNode.getServerArray().get();
            String[] namespaceArray = serverNode.getNamespaceArray().get();

            logger.info("ServerArray={}", Arrays.toString(serverArray));
            logger.info("NamespaceArray={}", Arrays.toString(namespaceArray));

            ServerStatusDataType serverStatus = serverNode.getServerStatus().get();
            logger.info("ServerStatus={}", serverStatus);


            ServerStatusTypeNode serverStatusNode = serverNode.getServerStatusNode().get();
            BuildInfo buildInfo = serverStatusNode.getBuildInfo().get();
            DateTime startTime = serverStatusNode.getStartTime().get();
            DateTime currentTime = serverStatusNode.getCurrentTime().get();
            ServerState state = serverStatusNode.getState().get();

            logger.info("ServerStatus.BuildInfo={}", buildInfo);
            logger.info("ServerStatus.StartTime={}", startTime);
            logger.info("ServerStatus.CurrentTime={}", currentTime);
            logger.info("ServerStatus.State={}", state);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    public String read(OpcUaClient client, NodeId nodeId) {
        try {
            DataValue value = client.readValue(0, TimestampsToReturn.Both, nodeId).get();
            if (value.getStatusCode().isGood()) {
                return value.getValue().getValue().toString();
            } else {
                return value.getStatusCode().toString();
            }
            //logger.error("ReadValue: Value={}",value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return "-/-";
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

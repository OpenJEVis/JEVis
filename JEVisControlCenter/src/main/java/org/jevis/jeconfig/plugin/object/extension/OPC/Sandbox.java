package org.jevis.jeconfig.plugin.object.extension.OPC;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.ServerStatusTypeNode;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.UaStackClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.l;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

public class Sandbox {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void connect() throws ExecutionException, InterruptedException {
        List<EndpointDescription> endpoints =
                DiscoveryClient.getEndpoints("opc.tcp://10.1.2.128:4840")
                        .get();

        EndpointDescription enpoinDes;
        endpoints.forEach(endpointDescription -> {
            try {
                System.out.println(endpointDescription);
                System.out.println("getSecurityLevel:\t\t" + endpointDescription.getSecurityLevel());
                System.out.println("getSecurityPolicyUri:\t" + endpointDescription.getSecurityPolicyUri());
                System.out.println("getSecurityMode:\t" + endpointDescription.getSecurityMode());
                System.out.println("getServerCertificate:\t" + endpointDescription.getServerCertificate());

                SecurityPolicy securityPolicy = SecurityPolicy.fromUri(endpointDescription.getSecurityPolicyUri());


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        enpoinDes = endpoints.get(0);
        try {
            System.out.println("Using endpoint: " + enpoinDes);

            OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
            cfg.setEndpoint(enpoinDes);

            System.out.println("done ClientConfig");

            try {
                OpcUaClient client = OpcUaClient.create(cfg.build());
                UaStackClient uaStackClient = client.getStackClient();

                UaClient uaclient = client.connect().get();
                BrowseDescription browseDescription = getBrowseDescription("", client, Identifiers.RootFolder);
                BrowseResult browseResult = client.browse(browseDescription).get();
                List<ReferenceDescription> references = toList(browseResult.getReferences());
                String indent = "";


                NodeId nodeIdNumeric = new NodeId(1, 20100);
                System.out.println(read(client, nodeIdNumeric));

                NodeId nodeIdName = NodeId.parse("ns=1;i=20036"); //20100
                System.out.println(read(client, nodeIdName));

                org.joda.time.DateTime from = new org.joda.time.DateTime(2020, 8, 26, 0, 0);
                org.joda.time.DateTime until = new org.joda.time.DateTime();
                //DateTime from= new DateTime(new Date());

                readHistory2(client, nodeIdName, new DateTime(from.toDate()), new DateTime(until.toDate()));

                /**
                 for (ReferenceDescription rd : references) {
                 logger.info("{} Node={}", indent, rd.getBrowseName().getName());
                 // recursively browse to children
                 rd.getNodeId().local().ifPresent(nodeId -> browseNode(indent + "-", client, nodeId));
                 }
                 **/

                test(client);
                System.out.println("done uaclient connect");
                uaclient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


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

    private void browseNode(String indent, OpcUaClient client, NodeId browseRoot) {
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


                if (rd.getNodeClass().equals(NodeClass.Variable)) {
                    NodeId nodeId = new NodeId(rd.getNodeId().getNamespaceIndex(), (UInteger) rd.getNodeId().getIdentifier());
                    logger.info("{} ns[{}] id[{}] c={} Node={} v={}", indent, rd.getNodeId().getNamespaceIndex(), rd.getNodeId().getIdentifier(), rd.getNodeClass(), rd.getBrowseName().getName(), read(client, nodeId));
                }

                if (rd.getNodeId().getIdentifier().toString().equals("20100")) {
                    logger.info("{} ns[{}] id[{}] c={} Node={} v={}", indent, rd.getNodeId().getNamespaceIndex(), rd.getNodeId().getIdentifier(), rd.getNodeClass(), rd.getBrowseName().getName(), read(client, rd.getReferenceTypeId()));
                    System.out.println(rd);
                    NodeId nodeIdName = NodeId.parse("ns=1;i=20100");
                    NodeId nodeId1 = rd.getReferenceTypeId();
                    NodeId nodeId2 = new NodeId(rd.getNodeId().getNamespaceIndex(), (UInteger) rd.getNodeId().getIdentifier());
                    System.out.println("r1: " + read(client, rd.getReferenceTypeId()));
                    System.out.println("r2: " + read(client, nodeId2));

                }

                if (rd.getBrowseName().getName().equals("Trend_System Temp11")) {
                    System.out.println("---------");
                    System.out.println("1:" + rd.getBrowseName().getName());
                    System.out.println("3:" + rd.getNodeId());
                    System.out.println("4:" + rd.getNodeId().getNamespaceIndex());
                    System.out.println("5:" + rd.getReferenceTypeId());
                    System.out.println("6:" + rd.getXmlEncodingId());
                    System.out.println("7:" + rd.getDisplayName());
                    System.out.println("8:" + rd.getNodeClass());

                    NodeId nodeIdNumeric = new NodeId(rd.getNodeId().getNamespaceIndex(), new Integer(rd.getNodeId().getIdentifier().toString()));


                    /**
                     try {
                     org.joda.time.DateTime from = new org.joda.time.DateTime(2020, 9, 26, 0, 0);
                     org.joda.time.DateTime until = new org.joda.time.DateTime();
                     //DateTime from= new DateTime(new Date());

                     readHistory(client, rd.getReferenceTypeId(), new DateTime(from.toDate()), new DateTime(until.toDate()));
                     } catch (Exception ex) {
                     ex.printStackTrace();
                     }
                     **/

                }

                // recursively browse to children
                rd.getNodeId().local().ifPresent(nodeId -> browseNode(indent + "-", client, nodeId));
            }
        } catch (Exception e) {
            logger.error("Browsing nodeId={} failed: {}", e);
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


    public List<HistoryReadValueId> readHistory2(OpcUaClient client, NodeId nodeId, DateTime from, DateTime until) throws ExecutionException, InterruptedException {

        List<HistoryReadValueId> ids = new ArrayList<>();

        //HistoryReadValueId id = new HistoryReadValueId(NodeId.parse("ns=4;s=Bucket Brigade.ArrayOfReal8"), 20100 + "", null, null);
        HistoryReadValueId id = new HistoryReadValueId(nodeId,
                20100 + "", null, null);

        ids.add(id);

        CompletableFuture<HistoryReadResponse> historyRead = client.historyRead(
                new ReadRawModifiedDetails(false, from, until, uint(1000), true),
                TimestampsToReturn.Server, false, ids);

        HistoryReadResponse response = historyRead.get();

        System.out.println("Size: "+response.getResults().length);


        for (HistoryReadResult result : response.getResults()) {
            System.out.println("Result: "+result.toString());
            System.out.println("Status: "+result.getStatusCode());
            HistoryData historyData = (HistoryData) result.getHistoryData().decode(
                    client.getSerializationContext()
            );
            for (DataValue dataValue : historyData.getDataValues()) {
                logger.error("ReadValue: Value={} seTime={} soTime={}", dataValue.getValue(), dataValue.getServerTime(), dataValue.getSourceTime());
            }


        }


        return null;
    }

    public List<HistoryReadValueId> readHistory(OpcUaClient client, NodeId nodeId, DateTime from, DateTime until) throws ExecutionException, InterruptedException {
        logger.error("start readout nodeId={} time: {}->{}", nodeId.getIdentifier(), from, until);

        //DataValue value = client.readValue(0, TimestampsToReturn.Both, nodeId).get();
        //logger.error("ReadValue: Value={} seTime={} soTime={}", value.getValue(), value.getServerTime(), value.getStatusCode());

        HistoryReadDetails hrd = new ReadRawModifiedDetails(false, from, until, UInteger.MAX, false);
        TimestampsToReturn ttr = TimestampsToReturn.Both;
        List<HistoryReadValueId> list = new ArrayList<>();
        //list.add(new HistoryReadValueId(new NodeId(2,"HelloWorld/ScalarTypes/Int32"), null, QualifiedName.NULL_VALUE,null));
        list.add(new HistoryReadValueId(nodeId, null, QualifiedName.NULL_VALUE, null));


        HistoryReadResponse hrr = client.historyRead(hrd, ttr, true, list).get();
        System.out.println("lenght: " + hrr.getResults().length);
        list.forEach(historyReadValueId -> {
            System.out.println("s: " + historyReadValueId);
        });
        System.out.println(".......");
        HistoryReadDetails historyReadDetails = new ReadRawModifiedDetails(
                false,
                DateTime.MIN_VALUE,
                DateTime.now(),
                uint(0),
                true
        );

/**
 List<HistoryReadValueId> historyReadValueIds = IntStream.rangeClosed(2019, 2020).mapToObj(value -> new HistoryReadValueId(
 nodeId,
 null,
 QualifiedName.NULL_VALUE,
 ByteString.NULL_VALUE
 )).collect(Collectors.toList());
 **/
        HistoryReadResponse historyReadResponse = client.historyRead(
                historyReadDetails,
                TimestampsToReturn.Server,
                false,
                list
        ).get();

        HistoryReadResult[] historyReadResults = historyReadResponse.getResults();

        if (historyReadResults != null) {
            HistoryReadResult historyReadResult = historyReadResults[0];
            HistoryData historyData = (HistoryData) historyReadResult.getHistoryData().decode(
                    client.getSerializationContext()
            );

            List<DataValue> dataValues = l(historyData.getDataValues());

            dataValues.forEach(v -> System.out.println("value=" + v));
        }


        return list;
    }

    public BrowseDescription getBrowseDescription(String indent, OpcUaClient client, NodeId browseRoot) {
        BrowseDescription browse = new BrowseDescription(
                browseRoot,
                BrowseDirection.Forward,
                Identifiers.References,
                true,
                uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
                uint(BrowseResultMask.All.getValue())
        );
        return browse;
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

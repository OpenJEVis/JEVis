package org.jevis.jeopc;

import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.HistoryReadResult;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class CMDClient {

    public CMDClient() {

    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Start CommandLine OPC UA Client using URL: " + args[0]);
        OPCClient opcClient = new OPCClient(args[0]);

        System.out.println("List Endpoints:");
        String username = "";
        String password = "";
        String dp = "";
        try {
            username = args[1];
            password = args[2];
            dp = args[3];
        } catch (Exception ex) {

        }
        int i = 0;
        int auto = -1;
        for (EndpointDescription endpointDescription : opcClient.getEndpoints()) {


            System.out.println(String.format("\nEP[%s]:  %s", i, endpointDescription.getEndpointUrl()));
            try {
                SecurityPolicy securityPolicy = SecurityPolicy.fromUri(endpointDescription.getSecurityPolicyUri());
                if (securityPolicy == SecurityPolicy.None) {
                    System.out.println("... Found Unsecured Endpoint");
                    auto = i;
                }
            } catch (UaException e) {
                e.printStackTrace();
            }
            String epString = OPCClient.printEP(endpointDescription);
            i++;
        }

        System.out.println("\nAutoselect EP: " + auto);

        Scanner scanner = new Scanner(System.in);
        EndpointDescription endpointDescription = null;
        try {
            while (true) {
                System.out.println("\n\nSelect endpoint ID, leave emtpy if same");
                String line = scanner.nextLine();

                if (!line.isEmpty()) {
                    endpointDescription = opcClient.getEndpoints().get(Integer.parseInt(line));
                }

                UsernameProvider usernameProvider = new UsernameProvider(username, password);
                opcClient.setEndpoints(endpointDescription);
                if (!username.isEmpty() && !password.isEmpty()) {
                    System.out.println("Useing Username/password: " + username + "/" + password);
                    opcClient.setIdentification(usernameProvider);
                }
                opcClient.connect();
                System.out.println("\nConnection successful");

                System.out.println("\nBrowse Device?(y/n)");
                String browse = scanner.nextLine();
                if (browse.toLowerCase().equals("y")) {
                    getDP(opcClient);
                }

                System.out.println("\nfetch samples?(type ID)");
                String fetch = scanner.nextLine();
                if (!browse.isEmpty()) {
                    printData(opcClient, fetch);
                }


            }
        } catch (Exception ex) {
            // System.in has been closed
            System.out.println("System.in was closed; exiting");
            ex.printStackTrace();
        }
    }


    private static void printData(OPCClient opcClient, String id) throws ExecutionException, InterruptedException {
        System.out.println("Fetch Datapoint: '" + id + "'");
        NodeId nodeIdName = NodeId.parse(id);//"ns=1;i=20036");

        DateTime from= new DateTime(2021,04,22,15,00,00);
        DateTime until= new DateTime(2021,04,22,16,00,00);

        //HistoryReadResult historyReadResult = opcClient.getHistory(nodeIdName, new DateTime().minusDays(1), new DateTime());
        OPCUAChannel OPCUAChannel = new OPCUAChannel(id,"i=11506",900000);

        HistoryReadResult historyReadResult = opcClient.getHistory(OPCUAChannel, from, until);
        List<DataValue> valueList = opcClient.getDateValues(historyReadResult);
        valueList.forEach(dataValue -> {
            try {
                System.out.println("sample: " + dataValue);
                //list.add(new DataValueRow(dataValue));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private static void getDP(OPCClient opcClient) {
        System.out.println("Fetch Nodes: please wait....");
        /**
         ObservableList<PathReferenceDescription> testList = FXCollections.observableArrayList();
         testList.addListener(new ListChangeListener<PathReferenceDescription>() {
        @Override public void onChanged(Change<? extends PathReferenceDescription> c) {
        while (c.next()) {
        if (c.wasAdded()) {
        c.getAddedSubList().forEach(o -> {
        //list.add(new Node(o.getReferenceDescription(), o.getPath()));
        // updateFilteredData();
        System.out.println("New Des: " + o.getPath() + "  -> " + o.getReferenceDescription().getBrowseName().getName());
        });

        }
        }

        }
        });
         **/
        HashMap<String, ReferenceDescription> map = opcClient.browse();
        map.forEach((s, referenceDescription) -> {
            System.out.println("New Des: " + referenceDescription.getBrowseName().getName() + " " + referenceDescription.getNodeId().toParseableString());
            System.out.println("-" + referenceDescription.toString());
            /**
             if (referenceDescription.getBrowseName().getName().equals("Strom - E-Mobility - Total Import (reactive) (Trend_Strom - E-Mobility - Total Import (reactive))")) {
             System.out.println("--Wait here");
             System.out.println("1 "+referenceDescription.());

             return;
             }
             **/
        });
        System.out.println("Done");
    }


}

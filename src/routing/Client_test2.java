/*
 * Client_test2.java
 *
 * Created on __Dec 14, 2013__, __2:14:45 AM__
 *
 * Copyright(c) {2013} Marcellin.  All Rights Reserved.
 * @author Marcellin Nshimiyimana<nshimiye@ovi.com>
 */

package routing;

import java.util.Hashtable;

/**
 *
 * @author mars
 */
public class Client_test2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
         Hashtable<String, Node_data> routingTB = new Hashtable<String, Node_data>();

        int node_port = 400,
                cost = 3;

        Node_data node = new Node_data("127.0.0.1", node_port, cost, true, null);

        //for now use ip:port as the id
        String node_name = node.getIp_addr() + ":" + String.valueOf(node.getPort());
        routingTB.put(node_name, node);

        System.out.printf("\ntb entry: %s\n", node_name);

        node = new Node_data("127.0.1.2", node_port + 100, cost, true, null);

        //for now use ip:port as the id
        node_name = node.getIp_addr() + ":" + String.valueOf(node.getPort());
        routingTB.put(node_name, node);

        node = routingTB.get("127.0.0.1:400");
Client nd1;
        System.out.printf("\ntb entry: %s = %s \n", node_name, node.createMsg("ROUTING UPDATE"));

        System.out.printf("\n\n\t\t\t\t------------test client-----------\n\n");
        // testing the client's methods
        // 128.59.196.2 20000 4.1 128.59.196.2 20001 5.2
         nd1 = new Client(20003, 5, "160.39.161.171 20000 0.1", "160.39.161.171 20009 0.1", "128.59.196.2 20001 0.1" );
         System.out.printf("\n%s\n", nd1.rTableForSND("128.59.196.2:20001"));
        nd1.showTable();
    }

}

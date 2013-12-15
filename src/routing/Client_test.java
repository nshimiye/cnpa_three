/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import java.util.Hashtable;

/**
 *
 * @author element
 */
public class Client_test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] arg) {


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

//        System.out.printf("\n\n\t\t\t\t------------test client-----------\n\n");
//        // testing the client's methods
//        // 128.59.196.2 20000 4.1 128.59.196.2 20001 5.2
//         nd1 = new Client(20001, 5, "160.39.161.171 20000 9.1");
        //nd1.showTable();


        System.out.printf("\n\n\t\t\t\t------------test client stage 2-----------\n\n");

        String[] args = "128.59.196.2 20000 4.1 128.59.196.2 20001 5.2 128.59.196.4 20000 3".split(" ");
        String[] st = null;
        if (args.length % 3 == 0) {
            st = new String[args.length / 3];
            for (int i = 0; i < st.length; i++) {
                st[i] = " " + args[i * 3] + " " + args[(i * 3) + 1] + " " + args[(i * 3) + 2] + " ";
                System.out.printf("%s\n", st[i]);
            }

        } else {
            System.err.println("possible input error");
            System.exit(-1);
        }
        // testing the client's methods
        // 128.59.196.2 20000 4.1 128.59.196.2 20001 5.2
        nd1 = new Client(20000, 5, st);

        System.out.printf("\n%s\n", nd1.rTableForSND("128.59.196.2:20001"));
        
        nd1.showTable();
    }
}

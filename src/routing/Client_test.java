/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import java.util.Hashtable;
import java.util.LinkedList;


/**
 *
 * @author element
 */
public class Client_test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
       Hashtable<String, Node_data> routingTB
               = new Hashtable<String, Node_data>();
        
        int node_port = 400,
                cost = 3;
        
       Node_data node = new Node_data("127.0.0.1", node_port, cost, true, null);
       
       //for now use ip:port as the id
       String node_name = node.getIp_addr() + ":"+ String.valueOf(node.getPort());
       routingTB.put(node_name, node);
       
       System.out.printf("\ntb entry: %s\n", node_name);
       
        node = new Node_data("127.0.1.2", node_port+100, cost, true, null);
       
       //for now use ip:port as the id
       node_name = node.getIp_addr() + ":"+ String.valueOf(node.getPort());
       routingTB.put(node_name, node);
       
       node = routingTB.get("127.0.0.1:400");
       
       System.out.printf("\ntb entry: %s = %s \n", node_name, node.createMsg("ROUTING UPDATE"));
       
    }
}

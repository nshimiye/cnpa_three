/**
 * class Client Marcellin Nshimiyimana
 */
package routing;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Marcellin
 */
public class Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    /**
     * method maintain : in charge of updating the routing table
     *
     */
    /**
     * sender thread will be in charge of sending whatever data it is given
     */
    /**
     * receiver thread will be in charge of receiving data sent to the client's
     * listening socket. And then using the processing method it will create a
     * Node_data object out of this data and save it to the routing table
     */
    /**
     * data_process method
     */
    private boolean debug = true;
    
    /**
     * initialize 
     * create first entries, they are supposed to be neighbors
     * format [{ type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, -, end}]
     */
    public boolean initialize(String [] node_msg){
        
        return false;
    }
    
    /**
     * create first entries, they are supposed to be neighbors
     * @param node_msg String
     * format [{ type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, -, end}]
     * @return 
     */
    public boolean msg_parser(String node_msg){
        boolean success = false;
        
        String [] entries = node_msg.split("::");
        //i am here???????
        
        return success;
    }

    /**
     * the bellman ford algorithm will be here update routing table here
     *
     * ========================== Each client maintains a distance vector, that
     * is, a list of <destination, cost> tuples, one tuple per client, where
     * cost is the current estimate of the total link cost on the shortest path
     * to the other client ========================== bellman-ford equation:
     * d_x(y) = min_v{ c(x,v) + d_v(y) } := routingTB.set(y)
     */
    public boolean update_dv(Hashtable<String, Node_data> inputDVEntry, Hashtable<String, Node_data> routingTB) {
        boolean success = false;
        //--- 

        //1. fill/update entries to which I(this client) am the source node
        //      we use the bellman-ford equation    

        //1.a update the ones that already exist in the table
        Node_data input = null, current = null;
        String in_name, curr_name;
        Enumeration<String> in_keys = inputDVEntry.keys();
        Enumeration<String> curr_keys = routingTB.keys();

        int count = 0, counttb = 0;
        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();

            if (debug) {//debug
                input = inputDVEntry.get(in_name);
                System.out.printf("name=<%s> [ count=%d] [size=%d]\n", input.myName(), count, inputDVEntry.size());
                count++;
            }

            //compare each entry of the input to the entries of the routing table
            counttb = 0;
            curr_keys = routingTB.keys();//to be opptimized????
            while (curr_keys.hasMoreElements()) {
                curr_name = curr_keys.nextElement();
                if (debug) {//debug

                    current = routingTB.get(curr_name); //--remove

                    System.out.printf("\t\tcurr: name=<%s> [ count=%d] [size=%d]\n", current.myName(), counttb, routingTB.size());
                    counttb++;
                }

                //check if same objects and then compute update
                if (in_name.equals(curr_name)) {
                    input = inputDVEntry.get(in_name);
                    current = routingTB.get(curr_name);
                    // d_x(y) = min_v{ c(x,v) + d_v(y) }

                    String src_name = input.getNh_ipaddr() + ":" + input.getNh_port();
                    Node_data src = routingTB.get(src_name);

                    if (src != null) { //each data entry has to have its sender already saved
                        int newcost = input.getCost_weight() + src.getCost_weight();
                        if ((!current.isLinkOn()) || (newcost < current.getCost_weight())) {//update if input's cost is less than current's one

                            if (debug) {//debug

                                System.out.printf("-----curr<link=%b>: name=<%s> [old=%d > ] [new=%d]-----\n",
                                        current.isLinkOn(), current.myName(), current.getCost_weight(), newcost);
                            }

                            //update the cost
                            input.setCost_weight(newcost);

                            //here i have to update the LFC for input

                            routingTB.put(curr_name, input);
                            //set sending flag on: not yet implemented??????????????????

                            //since each entry exist once, then finding means that
                            // we'r done searching, so ew can break inner loop
                            inputDVEntry.remove(input.myName());

                            break;

                        } //if names are different, i just go on
                    }
                }
            }
        }

        //1.b add newer nodes: the rest on nodes in the inputDVEntry
        if (inputDVEntry.size() > 0) {
            in_keys = inputDVEntry.keys();
            while (in_keys.hasMoreElements()) {
                in_name = in_keys.nextElement();


                //if the entry is not found, then add it
                current = routingTB.get(in_name);
                if (current == null) {
                    input = inputDVEntry.get(in_name);
                    String src_name = input.getNh_ipaddr() + ":" + input.getNh_port();
                    Node_data src = routingTB.get(src_name);

                    int newcost = input.getCost_weight() + src.getCost_weight();

                    if (debug) {//debug

                        System.out.printf("-----curr<link=%b>: name=<%s> [old=%d > ] [new=%d]-----\n",
                                current.isLinkOn(), current.myName(), current.getCost_weight(), newcost);

                    }

                    input.setCost_weight(newcost);
                    //here i have to create and assign the LFC for input

                    //add
                    routingTB.put(in_name, input);

                    //remove
                    inputDVEntry.remove(in_name);

                }
            }

        }
        //2. for each node(except me), compute cost using bellman-ford equation


        //---
        return success;
    }

    public String tableToString() {
        String completeTB = "";

        completeTB += ;
        
        return "";
    }

    public void showTable() {
    }
}

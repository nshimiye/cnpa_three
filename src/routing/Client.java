/**
 * class Client Marcellin Nshimiyimana
 */
package routing;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Marcellin
 */
public class Client {

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
    private boolean debug = false;
    private Hashtable<String, Node_data> rTable = null;
    private Node_data my_data = null;
    private SND_thread sender = null;
    private long timer;

    public Client(int myPort, int send_timer, String... node_data) {
        rTable = new Hashtable<String, Node_data>();
        timer = send_timer*1000;


        String nodes = "",
                myIpaddr = "";
        try {
            Enumeration iface = NetworkInterface.getNetworkInterfaces();
            while (iface.hasMoreElements()) {
                NetworkInterface net = (NetworkInterface) iface.nextElement();
                if (debug) {
                    System.err.printf("%s", net.toString());
                }
                if (!net.isLoopback()) {
                    Enumeration conn_addr = net.getInetAddresses();
                    //System.err.printf("oops in!!-%s\n",net.toString());
                    
                    if (conn_addr.hasMoreElements()) {
                        try {
                            Inet4Address addr = (Inet4Address) conn_addr.nextElement();
                            if (debug) {
                                System.err.printf("++++++-%s\n", addr.toString());
                            }
                            myIpaddr = addr.getHostAddress();

                            if (debug) {
                                System.err.printf("===%s\n", addr.getHostName());
                            }

                            break;
                        } catch (ClassCastException ex) {
                            //catch ipv6, which can not be cast to ipv4
                        }
                    }
                }
                if (debug) {
                    System.err.printf("\n");
                }

            }
        } catch (Exception ex) {
            System.err.printf("%s\n", ex.toString());
            myIpaddr = "127.0.0.1";
        }
        String nd;
        for (int i = 0; i < node_data.length - 1; i++) {
            nd = node_data[i].trim().replace(' ', ',');
            nodes += "INIT, " + nd + ", true, " + nd + " :: ";

        }

        //the last entry with no "::"
        if (node_data.length > 0) {
            nd = node_data[node_data.length - 1].trim().replace(' ', ',');
            nodes += "INIT, " + nd + ", true, " + nd;
        }

        //create my data 
        my_data = new Node_data(myIpaddr, myPort, 0, false, null);
        my_data.setNh_ipaddr(myIpaddr);
        my_data.setNh_port(myPort);

        //create the sender
        // Thread sender = null; new SND_thread(this, ngb_addrs);

        //create the receiver

        //create the command manager

        //initialize node table with neighbors
        initialize(nodes);
    }

    /**
     * initialize create first entries, they are supposed to be neighbors format
     *
     * @param node_msg string of the form [{ type, ip_addr, port, cost,
     * isNeighbor, nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, -,
     * end}]
     * @return boolean true if init succeeded, false otherwise
     */
    public boolean initialize(String node_msg) {
        if (debug) {//debug

            System.out.printf(">>>>>> [%s]\n", node_msg);
        }
        Hashtable<String, Node_data> inputDVector = msg_parser(node_msg);

        boolean status = update_dv(inputDVector, getrTable());

        return status;
    }

    public Node_data get_myData() {

        return my_data;
    }

    /**
     * create first entries, they are supposed to be neighbors
     *
     * @param node_msg String format [{ type, ip_addr, port, cost, isNeighbor,
     * nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, -, end}]
     * @return
     */
    public Hashtable<String, Node_data> msg_parser(String node_msg) {
        boolean success = false;
        Hashtable<String, Node_data> inputDV = new Hashtable<String, Node_data>();
        //for now "type" entry is not in use: assume to always be ROUTE UPDATE
        //[{ type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, 0, end} :: ...]
        String node_msg_tmp = node_msg.replace('[', ' ');
        node_msg_tmp = node_msg_tmp.replace(']', ' ');
        String[] entries = node_msg_tmp.split("::");

        //i am here???????
        for (int i = 0; i < entries.length; i++) {
            //{ type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end}
            String entry_tmp = entries[i];
            if (debug) {// no debug 

                System.out.printf(">->->->-> [%s]\n", entry_tmp);
            }
            entry_tmp = entry_tmp.replace('{', ' ');
            entry_tmp = entry_tmp.replace('}', ' ');

            String[] entry = entry_tmp.split(",");

            if (entry.length != 8) { //make sure this string array has 8 entries
                continue;
            }
            //create the Node_data object

            //sanityCheck(entry); =====   Assume data ssent in a nice way????????
            String ipaddr = entry[1].trim();
            int port = Integer.valueOf(entry[2].trim());
            double cost = Double.valueOf(entry[3].trim());

            boolean isngb = Boolean.valueOf(entry[4].trim());//false if -
            String nh_addr = entry[5].trim(); //default = -
            int nh_port = Integer.valueOf(entry[6].trim()); //default = 0

            Thread lfc = null;
            //=========================

            Node_data new_item = new Node_data(ipaddr, port, cost, isngb, lfc);
            new_item.setNh_ipaddr(nh_addr);
            new_item.setNh_port(nh_port);
            new_item.setLinkOn(true); //they by being on

            inputDV.put(new_item.myName(), new_item);
            if (debug) {//debug
                Node_data input = inputDV.get(new_item.myName());
                System.out.printf("name=<%s> [ alive=%b] [nghbor=%b] [LFC=%b] [nh=<%s:%d>] [size=%d]\n", input.myName(), input.isLinkOn(),
                        input.isNeighbor(), input.LFC_isAlive(), input.getNh_ipaddr(), input.getNh_port(), inputDV.size());
            }

        }
        return inputDV;
    }

    /**
     * the bellman ford algorithm will be here update routing table here
     *
     * ========================== Each client maintains a distance vector, that
     * is, a list of <destination, cost> tuples, one tuple per client, where
     * cost is the current estimate of the total link cost on the shortest path
     * to the other client ========================== bellman-ford equation:
     * d_x(y) = min_v{ c(x,v) + d_v(y) } := routingTB.set(y)
     *
     * work on link on/off ????????????????????
     */
    public boolean update_dv(Hashtable<String, Node_data> inputDVEntry, Hashtable<String, Node_data> routingTB) {
        boolean success = false,
                allow_send = true;
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
                        double newcost = input.getCost_weight() + src.getCost_weight();
                        if ((!current.isLinkOn()) || (newcost < current.getCost_weight())) {//update if input's cost is less than current's one

                            if (debug) {//debug

                                System.out.printf("-----curr<link=%b>: name=<%s> [old=%f > ] [new=%f]-----\n",
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

                    if (src != null || input.isNeighbor()) {
                        double newcost = 0;
                        if (input.isNeighbor()) { //isnotnull
                            newcost = input.getCost_weight();
                        } else { //isneighbor
                            newcost = input.getCost_weight() + src.getCost_weight();
                        }

                        if (debug) {//debug
                            System.out.printf("-----new <link=%b>: name=<%s> [old=%f > ] [new=%f]-----\n",
                                    input.isLinkOn(), input.myName(), input.getCost_weight(), newcost);
                        }

                        input.setCost_weight(newcost);
                        //here i have to create and assign the LFC for input

                        //add
                        routingTB.put(in_name, input);
                    }
                    //remove
                    inputDVEntry.remove(in_name);
                }
            }

        }
        //--???2. for each node(except me), compute cost using bellman-ford equation

        //if there has been a change in the table, we call the sender

        if (allow_send) {//rtb_updated
            in_keys = routingTB.keys();
            String ngb_addrs = "";
            Node_data inTable = null;
            while (in_keys.hasMoreElements()) {
                in_name = in_keys.nextElement();
                inTable = routingTB.get(in_name);
                if (inTable.isNeighbor() && inTable.isLinkOn()) {
                    ngb_addrs += ((ngb_addrs.trim().equals("")) ? "" : ",") + inTable.myName();
                }
            }

            if (sender != null) { //interrupt
                sender.setNode_ns(ngb_addrs);
                sender.interrupt();
            } else { //create and start it
                sender = new SND_thread(this, ngb_addrs, timer);
                sender.start();
            }
        }
        //---
        return success;
    }

    /**
     * Destination = 128.59.196.2:20000, Cost = 4.1, Link = (128.59.196.2:20000)
     *
     * @return
     */
    public String tableToString() {
        String completeTB = "";
        Node_data nd = null;

        Iterator<Map.Entry<String, Node_data>> tb_tmp = getrTable().entrySet().iterator();

        while (tb_tmp.hasNext()) {
            nd = tb_tmp.next().getValue();
            completeTB += "Destination = " + nd.myName()
                    + ", Cost = " + nd.getCost_weight()
                    + ", Link = (" + nd.getNh_ipaddr() + ":" + nd.getNh_port() + ")\n";
        }
        return completeTB;
    }

    /**
     * creating a string object representing the message to be sent the format
     * is [{msg1}::{msg2}::{msg3}...] Note: first message "msg1" must contain
     * information about the sender_node
     *
     * @return String
     *
     */
    public String rTableForSND() {
        String srtable = "[", in_name;

        Node_data inTable = null, inTable_tmp;

        /*
         * add self info to the message
         * Note: for now we assume that, sender send its cost
         *          (same for all neighbors for now and is 1) to the neighbors
         *         and it gets used when the sender is joining the net
         */
        inTable = get_myData();
        inTable_tmp = new Node_data(inTable.getIp_addr(), inTable.getPort(),
                inTable.getCost_weight() + 1, true, null);
        //i am the sender, so i have to update nh_info
        inTable_tmp.setNh_ipaddr(get_myData().getIp_addr());
        inTable_tmp.setNh_port(get_myData().getNh_port());
        srtable += inTable_tmp.createMsg("ROUTING UPDATE");

        Hashtable<String, Node_data> inputDVEntry = getrTable();
        Enumeration<String> in_keys = inputDVEntry.keys();
        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();

            //if the entry is not found, then add it
            inTable = inputDVEntry.get(in_name);
            if (inTable.isLinkOn()) { //send entries that report "LinkOn"
                inTable_tmp = new Node_data(inTable.getIp_addr(), inTable.getPort(),
                        inTable.getCost_weight(), false, null); //i assume that my neighbors are not other's neighbors
                //i am the sender, so i have to update nh_info
                inTable_tmp.setNh_ipaddr(get_myData().getIp_addr());
                inTable_tmp.setNh_port(get_myData().getNh_port());

                srtable += ("::") + inTable_tmp.createMsg("ROUTING UPDATE");
            }

        }
        srtable += "]"; //closing msg string

        return srtable;

    }

    public void showTable() {
        System.out.println("=========  routing table  =========");

        System.out.printf("%s", tableToString());

        System.out.println("=========  end  =========");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length % 3 == 0) {
            String[] st = new String[args.length / 3];
            for (int i = 0; i < args.length; i += 3) {
                st[i] = args[i] + args[i + 1] + args[i + 2];

            }

        } else {
            System.err.println("possible input error");
            System.exit(-1);
        }
    }

    /**
     * @return the rTable
     */
    public Hashtable<String, Node_data> getrTable() {
        return rTable;
    }
}

/**
 * class Client Marcellin Nshimiyimana
 */
package routing;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Hashtable;

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
    private RCV_thread receiver = null;
    LFC_thread lfc = null;
    private long timer = 3000;
    private boolean allow_send = false; //used to make allow sending route updates
    private final int MAX_UDP = 1024;
    private final double INF = 500;
    private String routing_msg = "ROUTING";

    /**
     * This is represents a node in a network
     *
     * @param myPort int : the listening port, on which neighbor send their
     * routing table info
     * @param send_timer long: the timeout for resending routing table info to
     * neighbors
     * @param node_data String: information about the neighbor of this client
     * node
     */
    public Client(int myPort, long send_timer, String... node_data) {
        rTable = new Hashtable<String, Node_data>();
        timer = send_timer * 1000;

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
        for (int i = 0; i < node_data.length; i++) {
            nd = node_data[i].trim().replace(' ', ',');
            nodes += "INIT, " + nd + ", true, " + nd + " :: ";

        }

        //the last entry with no "::"
//        if (node_data.length > 0) {
//            nd = node_data[node_data.length - 1].trim().replace(' ', ',');
//            nodes += "INIT, " + nd + ", true, " + nd;
//        }

        //create my data 
        my_data = new Node_data(myIpaddr, myPort, 0, false, null, this);
        my_data.setNh_ipaddr(myIpaddr);
        my_data.setNh_port(myPort);
        my_data.setLinkOn(false);
        my_data.setLFC(null);
        nd = my_data.getIp_addr() + "," + my_data.getPort() + "," + my_data.getCost_weight();
        nodes += "INIT, " + nd.trim() + ", false, " + nd.trim();
        //create the sender
        // Thread sender = null; new SND_thread(this, ngb_addrs);
        //initialize node table with neighbors
        initialize(nodes);

        //create the receiver
        receiver = new RCV_thread(this, myPort);

        //after init then we can start receiving
        receiver.start();
    }

    /**
     * initialize create first entries, they are supposed to be neighbors format
     *
     * @param node_msg string of the form [{ type, ip_addr, port, cost,
     * isNeighbor, nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, -,
     * end}]
     * @return boolean true if init succeeded, false otherwise
     */
    private boolean initialize(String node_msg) {
        if (debug) {//debug
            System.out.printf("[initialize]: >>>>>> [%s]\n", node_msg);
        }
        Node_data neighbor = null;
        //i have to be in the table first
        rTable.put(my_data.myName(), my_data);
        Hashtable<String, String> inputDVector = msg_parserV2(node_msg);

        // String nhname, nhnamecmp;
        String[] nhname_tmp;

        String in_name;
        String[] names; //names: name split
        double ncost = 0;
        Enumeration<String> ngh_keys = inputDVector.keys();

        while (ngh_keys.hasMoreElements()) {
            in_name = ngh_keys.nextElement();
            if (!in_name.equals(my_data.myName())) {//do this for neghbors


                nhname_tmp = inputDVector.get(in_name).split("::"); //costSent;;
                ncost = Double.valueOf(nhname_tmp[0].trim());
                //nhname = nhname_tmp[1].trim();
                //new

                names = in_name.split(":");
                String nghIpaddr = names[0]; //possible error
                int nghPort = Integer.valueOf(names[1]); //possible error

                //add neighbors to rtable here
                neighbor = new Node_data(nghIpaddr, nghPort, ncost, true, null, this);

                neighbor.setNh_ipaddr(nghIpaddr);
                neighbor.setNh_port(nghPort);
                neighbor.setLinkOn(true);
                neighbor.setIsneighbor(true);

                rTable.put(neighbor.myName(), neighbor);

                if (!neighbor.LFC_isAlive()) {
                    lfc = new LFC_thread(this, neighbor, this.getTimer());
                    neighbor.setLFC(lfc);
                } else {
                    neighbor.getLFC().interrupt();
                }
                if (debug) {
                    System.out.printf("[init -%d-]: %s>>>>>> [%s]\n", rTable.size(), neighbor.myName(), neighbor.createSND("INIT"));
                }
            }
        }

        if (debug) {
            System.out.printf("[init -%d-]: %s>outside before [%s]\n\n\n", rTable.size(), my_data.myName(), my_data.createSND("INIT"));
        }
        //since we know ourselves only, we can update
        boolean status = update_rt(my_data.myName(), inputDVector);

        if (debug) {
            System.out.printf("[init ]\n\n\n");

            System.out.printf("[init ]: %s>==< [%s]\n", get_myData().myName(), get_myData().createSND("INIT"));
        }

        if (debug) {
            ngh_keys = rTable.keys();
            System.out.printf("[init ]\n\n\n");
            while (ngh_keys.hasMoreElements()) {
                in_name = ngh_keys.nextElement();

                System.out.printf("[init -%d-]: %s> [%s]\n", rTable.size(), rTable.get(in_name).myName(), rTable.get(in_name).createSND("INIT"));
            }
        }
        return status;
    }

    /**
     *
     * @param sender_name String :name(<ip_addr:port>) of the node, sender of
     * message "node_msg"
     * @param node_msg String :message received via UDP protocol
     * @return
     */
    public boolean reInit(String sender_name, String node_msg) {
        boolean status = false;
        if (debug) {// no debug
            System.out.printf("[reInit]: %s>>>>>> [%s]\n", sender_name, node_msg);
        }

        Hashtable<String, String> inputDVector = msg_parserV2(node_msg);

        String[] nhname_tmp;

        if (debug) {
            Enumeration<String> ngh_keys = inputDVector.keys();
            System.out.printf("[init ]\n\n\n");
            while (ngh_keys.hasMoreElements()) {
                String in_name = ngh_keys.nextElement();

                System.out.printf("[init -%d-]: %s> [%f]\n", inputDVector.size(), in_name, inputDVector.get(in_name));
            }
        }

        Node_data new_ngb = rTable.get(sender_name);
        double ncost = 1000;
        if (new_ngb != null) {


            nhname_tmp = inputDVector.get(sender_name).split("::"); //costSent;;
            ncost = Double.valueOf(nhname_tmp[0].trim());


            new_ngb.setCost_weight(ncost);
            new_ngb.setLinkOn(true);
            new_ngb.setIsneighbor(true);
            if (new_ngb.LFC_isAlive()) {
                new_ngb.getLFC().interrupt();
            } else {
                lfc = new LFC_thread(this, new_ngb, this.getTimer());
                new_ngb.setLFC(lfc);
            }


        } else {//it is a new node, so we save it


            if (debug) {
                System.err.printf("[reInit]: rcved[%s] msg=[%s]\n", sender_name, node_msg);
            }


            nhname_tmp = inputDVector.get(sender_name).split("::"); //costSent;;
            ncost = Double.valueOf(nhname_tmp[0].trim());


            String[] names = sender_name.split(":");
            String nghIpaddr = names[0]; //possible error
            int nghPort = Integer.valueOf(names[1]); //possible error

            //add neighbors to rtable here
            Node_data neighbor = new Node_data(nghIpaddr, nghPort, ncost, true, null, this);
            neighbor.setNh_ipaddr(nghIpaddr);
            neighbor.setNh_port(nghPort);
            neighbor.setLinkOn(true);
            neighbor.setIsneighbor(true);

            rTable.put(neighbor.myName(), neighbor);

            neighbor = rTable.get(neighbor.myName());
            //set sender permission on
            if (neighbor.LFC_isAlive()) {//unlikely to happen
            } else {
                lfc = new LFC_thread(this, neighbor, this.getTimer());
                neighbor.setLFC(lfc);
            }
        }

        //since we know ourselves only, we can update
        status = update_rt(sender_name, inputDVector);


        if (debug) {
            Enumeration<String> ngh_keys = rTable.keys();
            System.out.printf("[init ]\n\n\n");
            while (ngh_keys.hasMoreElements()) {
                String in_name = ngh_keys.nextElement();

                System.out.printf("[init -%d-]: %s> [%s]\n", rTable.size(), rTable.get(in_name).myName(), rTable.get(in_name).createSND("INIT"));
            }
        }

//            } else {
//                if (debug) {//no debug
//                    System.out.printf("[reInit inside]: wrong message[%s], no update made \n", node_msg);
//                }
//            }
//
//        } else {
//            if (debug) {// no debug
//                System.out.printf("[reInit outside]: wrong message[%s], no update made \n", node_msg);
//            }
//        }

        return status;
    }

    /**
     * Update v2 give access to the data of this node Note: node address and its
     * nh's address are the same (nh: next hope)
     *
     * @return Node_data
     */
    public Node_data get_myData() {
        Hashtable<String, Node_data> rt = getrTable();
        Node_data tmp = rt.get(my_data.myName());
        if (tmp == null) {
            tmp = my_data;
        }
        return tmp; //hopefully they will sync
    }

    public Hashtable<String, String> msg_parserV2(String node_msg) {
        Hashtable<String, String> inputDV = new Hashtable<String, String>();

        //for now "type" entry is not in use: assume to always be ROUTE UPDATE
        //[{ type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, 0, end} :: ...]
        String node_msg_tmp = node_msg.replace('[', ' ');
        node_msg_tmp = node_msg_tmp.replace(']', ' ');
        String[] entries = node_msg_tmp.split("::");

        String nhname, nhnamecmp;

        for (int i = 0; i < entries.length; i++) {
            //{ type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end}
            String entry_tmp = entries[i];
            if (debug) {// no debug 
                System.out.printf(">->->->-> [%s]\n", entry_tmp);
            }
            entry_tmp = entry_tmp.replace('{', ' ');
            entry_tmp = entry_tmp.replace('}', ' ');

            String[] entry = entry_tmp.split(",");

            if (entry.length != 8) { //make sure this string array has 9 entries
                continue;
            }

            String ipaddr = entry[1].trim();
            String ports = entry[2].trim();
            int port = Integer.valueOf(ports);
            double cost = Double.valueOf(entry[3].trim());
            String nd_name = ipaddr + ":" + ports;

            //entry[5].trim() nhIp
            //entry[6].trim() nhPort
            nhname = entry[5].trim() + ":" + entry[6].trim();
            nhnamecmp = String.format("%.1f", cost) + "::" + nhname;

            inputDV.put(nd_name, nhnamecmp);

            if (debug) {// no debug 
                System.out.printf(">->->->-> [%s === %s]\n", entry_tmp, inputDV.get(nd_name));
            }
        }


        return inputDV;
    }

    /**
     * Creating routing table entries from the string object (ROUTE UPDATE's)
     *
     * @param node_msg String format [{ type, ip_addr, port, cost, isNeighbor,
     * nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, -, end}]
     * @return
     */
    public Hashtable<String, Node_data> msg_parser(String node_msg) {

        Hashtable<String, Node_data> inputDV = new Hashtable<String, Node_data>();
        //for now "type" entry is not in use: assume to always be ROUTE UPDATE
        //[{ type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end} :: { -, ip_addr, port, cost, -, -, 0, end} :: ...]
        String node_msg_tmp = node_msg.replace('[', ' ');
        node_msg_tmp = node_msg_tmp.replace(']', ' ');
        String[] entries = node_msg_tmp.split("::");


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

            LFC_thread lfc = null;
            //=========================

            Node_data new_item = new Node_data(ipaddr, port, cost, isngb, lfc, this);
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
     * Update v2
     *
     * @param sender_name
     * @param inputEntries
     * @return
     */
    public boolean update_rt(String sender_name, Hashtable<String, String> inputEntries) {
        boolean success = false;
        Node_data nd = null;
        String in_name;

        Hashtable<String, Node_data> tb_tmp = getrTable();

        Enumeration<String> curr_keys = tb_tmp.keys();

        while (curr_keys.hasMoreElements()) {
            in_name = curr_keys.nextElement();

            if (in_name.equals(get_myData().myName())) {

                //1. update my info
                nd = tb_tmp.get(get_myData().myName());
                success = nd.updatedv(inputEntries);

                if (debug) { //no debug
                    System.out.printf("[updateV2 ]: %s>==before?=== [%s]\n", nd.myName(), tb_tmp.get(get_myData().myName()).createSND("OUT"));
                }
                setAllow_send(success);

                success = nd.cleanUp("INF", inputEntries);
                if (!allow_send) {
                    setAllow_send(success);
                }
                if (debug) { //no debug
                    System.out.printf("[updateV2 ]: %s>===after?== [%s]\n", nd.myName(), nd.createSND("OUT"));
                }

            } else if (in_name.equals(sender_name)) {

                nd = tb_tmp.get(sender_name);

                success = nd.replaceTB(inputEntries);

                if (!allow_send) {
                    setAllow_send(success);
                }

                if (debug) { //no debug
                    System.out.printf("[updateV2 ]: %s>==????????????=== [%s]\n", get_myData().myName(), tb_tmp.get(get_myData().myName()).createSND("OUT"));
                }

            } else { //this is for making sure each neighbor has same number of neighbors
                nd = tb_tmp.get(in_name);
                nd.addWondersV2(inputEntries);
            }

        }

        success = true;
        //if there has been a change in the table, we call the sender
        if (allow_send) {//rtb_updated

            String ngb_addrs = getNeighbors();

            if (getSender() != null) { //interrupt
                getSender().setNode_ns(ngb_addrs);
                getSender().interrupt();

            } else { //create and start it
                sender = new SND_thread(this, ngb_addrs, getTimer());
                getSender().start();

            }
            if (debug) { //debug
                System.out.printf("[updateV2 ]: %s>==allowsend [%s]\n", get_myData().myName(), tb_tmp.get(get_myData().myName()).createSND("OUT"));
            }

//            turn the send pprmission off
            setAllow_send(false);
            success = true;
        }

        if (debug) { //no debug
            System.out.printf("[updateV2 ]: %s>==lastbut One=== [%s]\n", get_myData().myName(), tb_tmp.get(get_myData().myName()).createSND("OUT"));
        }

        return success;
    }

    //not yet?????????????????
    public String[] getAllNodes() {
        String[] allNodes = new String[1];


        return allNodes;
    }

    /**
     * To be removed??????????????? the bellman ford algorithm will be here
     * update routing table here
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
        boolean success = false;

        //--- 

        //1. fill/update entries to which I(this client) am the source node
        //      we use the bellman-ford equation    

        //1.a update the ones that already exist in the table
        Node_data input = null, input_tmp = null, current = null;
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
                    input_tmp = inputDVEntry.get(in_name);
                    input = new Node_data(input_tmp.getIp_addr(), input_tmp.getPort(),
                            input_tmp.getCost_weight(), input_tmp.isNeighbor(), input_tmp.getLFC(), this);

                    input.setNh_ipaddr(input_tmp.getNh_ipaddr());
                    input.setNh_port(input_tmp.getNh_port());
                    input.setLinkOn(true);

                    if (debug) {
                        System.out.printf("[updatedv]: %s\n", input.nhName());
                    }

                    current = routingTB.get(curr_name);
                    // d_x(y) = min_v{ c(x,v) + d_v(y) }

                    Node_data src = routingTB.get(input.nhName());

                    if (src != null) { //each data entry has to have its sender already saved
                        double newcost = input.getCost_weight() + src.getCost_weight();
                        if ((!current.isLinkOn()) || (newcost < current.getCost_weight())) {//update if input's cost is less than current's one

                            if (debug) {//debug

                                System.out.printf("-----curr<link=%b>: name=<%s> [old=%f > ] [new=%f]-----\n",
                                        current.isLinkOn(), current.myName(), current.getCost_weight(), newcost);
                            }

                            //update the cost
                            if (current.isLinkOn()) {
                                input.setCost_weight(newcost);
                            }

                            //here i have to update the LFC for input

                            routingTB.remove(curr_name);
                            routingTB.put(curr_name, input);
                            setAllow_send(true);
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

                    input_tmp = inputDVEntry.get(in_name);
                    input = new Node_data(input_tmp.getIp_addr(), input_tmp.getPort(),
                            input_tmp.getCost_weight(), input_tmp.isNeighbor(), input_tmp.getLFC(), this);

                    input.setNh_ipaddr(input_tmp.getNh_ipaddr());
                    input.setNh_port(input_tmp.getNh_port());
                    input.setLinkOn(true);

                    Node_data src = routingTB.get(input.nhName());

                    if (src != null || input.isNeighbor()) {
                        double newcost = 0;
                        if (input.isNeighbor()) { //isneighbor
                            newcost = input.getCost_weight();

                            lfc = new LFC_thread(this, input, this.getTimer());
                            input.setLFC(lfc);

                        } else { //isnotnull
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
                        setAllow_send(true);
                    }
                    //remove
                    inputDVEntry.remove(in_name);
                }
            }

        }
        //--???2. for each node(except me), compute cost using bellman-ford equation

        //if there has been a change in the table, we call the sender
        if (allow_send) {//rtb_updated

            String ngb_addrs = getNeighbors();


            if (getSender() != null) { //interrupt
                getSender().setNode_ns(ngb_addrs);
                getSender().interrupt();
            } else { //create and start it
                sender = new SND_thread(this, ngb_addrs, getTimer());
                getSender().start();
            }

//            turn the send pprmission off
            setAllow_send(false);
        }
        success = true;
        //showTable();//-- to be removed
        //---
        return success;
    }

    /**
     *
     * @return
     */
    public String getNeighbors() {

        Enumeration<String> in_keys = rTable.keys();
        String ngb_addrs = "", in_name;
        Node_data inTable = null;

        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();
            inTable = rTable.get(in_name);
            if (rTable.get(in_name).isNeighbor() && rTable.get(in_name).isLinkOn() && (!rTable.get(in_name).myName().equals(get_myData().myName()))) {
                ngb_addrs += ((ngb_addrs.trim().equals("")) ? "" : ",") + inTable.myName();
            }
        }

        return ngb_addrs;
    }

    /**
     * Destination = 128.59.196.2:20000, Cost = 4.1, Link = (128.59.196.2:20000)
     *
     * @return
     */
    public String tableToString() {
        String completeTB = "", mynd = "";
        double myCost = 0;

        String nhname;
        String[] nhname_tmp;

//        get all reachable nodes
        Hashtable<String, String> rnodes = get_myData().getDvector();

        Enumeration<String> in_keys = rnodes.keys();

        while (in_keys.hasMoreElements()) {
            mynd = in_keys.nextElement();

            //loop through all nodes and check entry that match the cost with mynd

            nhname_tmp = rnodes.get(mynd).split("::"); //costSent;;
            myCost = Double.valueOf(nhname_tmp[0].trim());
            nhname = nhname_tmp[1].trim();

            if (!get_myData().myName().equals(mynd)) {// we only search for nodes other than me

                Node_data nh = getrTable().get(nhname);
                if (debug) {
                    if (nh != null) {
                        System.err.printf("[updatertNode ]: %s:%b>==before?=== [%s]\n", nh.myName(), nh.isLinkOn(), get_myData().createSND("OUT"));
                    }
                }
                if (nh != null) {

                    if ((nh.isNeighbor() && nh.isLinkOn()) && (myCost < INF)) {//we have to printf complete table to make sure

                        completeTB += "Destination = " + mynd
                                + ", Cost = " + String.format("%.1f", myCost)
                                + ", Link = (" + nh.getNh_ipaddr() + ":" + nh.getNh_port() + ")\n";

                    }
                }
            }

        }
        return completeTB;
    }

    /**
     * Update v2 creating a string object representing the message to be sent
     * the format is [{msg1}::{msg2}::{msg3}...] Note: first message "msg1" must
     * contain information about the sender_node
     *
     * @return String
     *
     */
    public String rTableForSND(String dst_name) {
        String srtable = "[";

        Node_data inTable;
        Hashtable<String, Node_data> inputDVEntry = getrTable();

        /*to be removed!!!!!!!!!!!!
         * add self info to the message
         * Note: for now we assume that, sender send its cost
         *          (same for all neighbors for now and is 1) to the neighbors
         *         and it gets used when the sender is joining the net
         */

        Node_data dst_tmp = inputDVEntry.get(dst_name);
        inTable = get_myData();

        inTable.setCost_weight(dst_tmp.getCost_weight());
        srtable += get_myData().createSND(routing_msg);
        srtable += "]"; //closing msg string

        return srtable;

    }

    public void showTable() {
        System.out.printf("=========  routing table  =========\n"
                + "%s\n"
                + "=========  end  =========\n", tableToString());
    }

    /**
     * @return the rTable
     */
    public Hashtable<String, Node_data> getrTable() {
        return rTable;
    }

    /**
     * @param allow_send boolean: the permission for SND_thread to send routing
     * update
     */
    public void setAllow_send(boolean allow_send) {
        this.allow_send = allow_send;
    }

    /**
     * @param routing_msg the routing_msg to set
     */
    public void setRouting_msg(String routing_msg) {
        this.routing_msg = routing_msg;
    }

    /**
     * @return the sender
     */
    public SND_thread getSender() {
        return sender;
    }

    /**
     * @return the receiver
     */
    public RCV_thread getReceiver() {
        return receiver;
    }

    /**
     * @return the timer
     */
    public long getTimer() {
        return timer;
    }

    /**
     * @param timer the timer to set
     */
    public void setTimer(long timer) {
        this.timer = timer;
    }
    /**
     * @param args the command line arguments
     */
    /*
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
     */
}

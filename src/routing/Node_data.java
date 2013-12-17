package routing;

import java.util.*;

/**
 * an object representation of data used by the distance vector just data
 * collected and saved in the routing table
 *
 * for a single connected/reachable node
 *
 * 2. it is better to allow the Node_data to create a part of the routing
 * message that has its data(ip, port, cost ...)
 */
public class Node_data {

    private String ip_addr;
    private int port;
    private double cost_weight;
    private boolean linkOn, isneighbor; //always false for Node_data that saves the creator's info (i.e. Cient who created)
    private LFC_thread LFC = null; //link failure checker
    //let's add the src where this data is coming from
    private String nh_ipaddr = "-"; //default
    private int nh_port = 0; //default
    private Hashtable<String, String> dvector = null; //<ip_addr:port>=cost
    private final double INF = 500;
    private Client meNode;
    private boolean debug = false;

    public Node_data(String ip_address, int node_port, double cost, boolean neighbor, LFC_thread LFC_used, Client me_node) {
        ip_addr = ip_address;
        port = node_port;
        cost_weight = cost;
        isneighbor = neighbor;
        LFC = LFC_used;
        meNode = me_node;
        dvector = new Hashtable<String, String>();

    }

    /**
     * shows the name <ip_addr:port> of the node whose data is represented by
     * this Node_data object
     *
     * @return String node_name, in the form ip_addr:port
     */
    public String myName() {
        String node_name = getIp_addr() + ":" + String.valueOf(getPort());
        return node_name;
    }

    /**
     * shows the name <ip_addr:port> of the next hope used to get to Node_data
     * owner
     *
     * @return String node_name, in the form ip_addr:port
     */
    public String nhName() {
        String node_name = getNh_ipaddr() + ":" + String.valueOf(getNh_port());
        return node_name;
    }

    /**
     * getDventry short for distance vector entry, this is very helpful when you
     * need to display routing info as described in the homework
     *
     * @return String <destination, cost> tuple for this(Node_data) data object
     */
    public String getDventry() {

        String dventry = ""; //entry <> for this linked node
        dventry += "<" + getIp_addr() + ", " + String.format("%.2f", getCost_weight()) + ">";

        return dventry;
    }

    /**
     * Just add entries that are reachable even if we don't know how this node
     * can reach it Cost is set to infinity obv
     *
     * @param newEntries
     * @return
     */
    public boolean addWondersV2(Hashtable<String, String> newEntries) {
        boolean succes = false;
        Enumeration<String> in_keys = newEntries.keys();
        String in_name;
        double newCost = INF;
        String nhname, nhnamecmp;
        String[] nhname_tmp;

        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();

            //if in_name is in dvector update its cost
            if (!getDvector().containsKey(in_name)) {

                nhname_tmp = newEntries.get(in_name).split("::"); //costSent;;
//                newCost = Double.valueOf(nhname_tmp[0].trim());
                nhname = nhname_tmp[1].trim();

                //newCost = newEntries.get(in_name); //costSent
                if (!in_name.equals(myName())) {//only if this is not the neighbor ... never false
                    newCost = INF; // + costToNeighbor
                }
                nhnamecmp = String.format("%.1f", INF) + "::" + nhname;
                getDvector().put(in_name, nhnamecmp);
                succes = true;
            }

        }
        return succes;
    }

    //Hashtable<String, Double>
    /**
     * Hashtable<ip_addr:port, cost::ip_addr:port>
     *
     * @param newEntries
     * @return
     */
    public boolean updatedv(Hashtable<String, String> newEntries) {
        boolean allow_send = false;

        double tmpCost = 0, newCost = 0;
        String nhname, nhnamecmp;
        String[] nhname_tmp;
        Node_data nh = null, curr_nh = null;
        //Iterator<Map.Entry<String, Double>> tb_tmp = dvector.entrySet().iterator();

        Enumeration<String> in_keys = newEntries.keys();
        String in_name;

        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();

            //if in_name is in dvector update its cost
            if (getDvector().containsKey(in_name)) {

                String tmpp = getDvector().get(in_name);

                if (tmpp != null) {
                    nhname_tmp = tmpp.split("::");
                    tmpCost = Double.valueOf(nhname_tmp[0].trim());
                    nhname = nhname_tmp[1].trim();
                    curr_nh = meNode.getrTable().get(nhname);

                    // d_x(y) = min_v{ c(x,v) + d_v(y) }
                    //new cost = costSent + costToNeighbor
                    nhname_tmp = newEntries.get(in_name).split("::"); //costSent;;
                    newCost = Double.valueOf(nhname_tmp[0].trim());
                    nhname = nhname_tmp[1].trim();
                    nh = meNode.getrTable().get(nhname);

                    if (debug) { //no debug
                        if (nh != null) {
                            System.err.printf("[updatertNode ]: %s>==before?=== [%s]\n", nh.myName(), nh.createSND("OUT"));
                        }
                    }
                    // if (nh != null) {

                    if (debug) {
                        if (nh != null) {
                            System.err.printf("[updatertNode ]: %s>==before?=== [%s]\n", nh.myName(), nh.createSND("OUT"));
                        }
                    }

                    if (!in_name.equals(myName())) {//only if this is not the neighbor
                        newCost += ((nh != null && (!nh.myName().equals(in_name))) ? nh.getCost_weight() : 0); // + costToNeighbor
                    } else {
                        newCost = 0;
                        nhname = myName();
                    }

                    if (newCost < INF) {
                        if (debug) {
                            System.err.printf("[updatertNode ]: >==before?=== [new%f <=> old%f]\n", newCost, tmpCost);
                        }
                        if ((newCost < tmpCost) || (!curr_nh.isLinkOn()) || (curr_nh.getCost_weight() >= INF)) {
                            //update the cost here
                            nhnamecmp = String.format("%.1f", newCost) + "::" + nhname;
                            getDvector().remove(in_name);
                            getDvector().put(in_name, nhnamecmp);
                            allow_send = true;
                        }
                    } else {
                        //update the cost here
                        nhnamecmp = String.format("%.1f", INF) + "::" + nhname;
                        getDvector().remove(in_name);
                        getDvector().put(in_name, nhnamecmp);
                        //allow_send = true;
                    }
                }

            } else {
                nhname_tmp = newEntries.get(in_name).split("::");
                newCost = Double.valueOf(nhname_tmp[0].trim()); //costSent
                nhname = nhname_tmp[1].trim();

                nh = meNode.getrTable().get(nhname);
                if (!in_name.equals(myName())) {//only if this is not the neighbor ... never false
                    newCost += ((nh != null && (!nh.myName().equals(in_name))) ? nh.getCost_weight() : 0); // + costToNeighbor
                }
                nhnamecmp = String.format("%.1f", newCost) + "::" + nhname;
                if (debug) {
                    System.err.printf("[updatertNode ]: %s>==before?=== [%s]\n", in_name, nhnamecmp);
                }
                getDvector().put(in_name, nhnamecmp);
                allow_send = true;
            }

        }

        return allow_send;
    }

    public boolean replaceTB(Hashtable<String, String> newEntries) {

        boolean allow_send = false;
        Enumeration<String> in_keys = newEntries.keys();
        String in_name;

        getDvector().clear();

        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();

            getDvector().put(in_name, newEntries.get(in_name));
            //allow_send = true;

        }

        return allow_send;
    }

    /**
     * uses global list of reachable nodes to make sure its entries(in the
     * dvector) are all valid/reachable If not each invalid entry is removed
     *
     * @param newEntries Hashtable<String, String> :global list of reachable
     * nodes, created by my clientNode ex: Hashtable<ip_addr:port,
     * cost::ip_addr:port>
     * @return
     */
    public boolean cleanUp(String flag, Hashtable<String, String> newEntries) {
        boolean allow_send = false;
        Enumeration<String> in_keys = getDvector().keys();
        String in_name;

        String nhname, nhnamecmp;
        String[] nhname_tmp;

        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();

            //if in_name is in dvector clean it up
            if (!newEntries.containsKey(in_name)) {
                String tmpp = getDvector().get(in_name);
                if (tmpp != null) {
                    nhname_tmp = tmpp.split("::");

                    nhname = nhname_tmp[1].trim();

                    if (flag.toUpperCase().equals("CLR")) {
                        //getDvector().remove(in_name);
                    } else if (flag.toUpperCase().equals("INF")) {
                        nhnamecmp = String.format("%.1f", INF) + "::" + nhname;
                        getDvector().remove(in_name);
                        getDvector().put(in_name, nhnamecmp);
                        allow_send = true;
                    }
                    //allow_send = true;
                }
            } else {
                String tmpp = newEntries.get(in_name);
                nhname_tmp = tmpp.split("::");

                nhname = nhname_tmp[1].trim();

                if (nhname.equals(myName())) {
                    //you do the same as up
                    nhnamecmp = String.format("%.1f", INF) + "::" + nhname;
                    getDvector().remove(in_name);
                    getDvector().put(in_name, nhnamecmp);
                    allow_send = true;
                }
            }
        }


        return allow_send;
    }

    public void closeLinks() {
        Enumeration<String> in_keys = getDvector().keys();
        String in_name;

        String nhname, nhnamecmp;
        String[] nhname_tmp;

        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();

            String tmpp = getDvector().get(in_name);
            if (tmpp != null) {
                nhname_tmp = tmpp.split("::");

                nhname = nhname_tmp[1].trim();

                nhnamecmp = String.format("%.1f", INF) + "::" + nhname;
                getDvector().remove(in_name);
                getDvector().put(in_name, nhnamecmp);

            }
        }
    }

    /**
     * format used is similar to json, except
     *
     * @param type String (ROUTE UPDATE, INIT, SELF) specifies the type of
     * message created (not important for now)
     * @return the message in the format { type, ip_addr, port, cost,
     * isNeighbor, nh_addr, nh_port, end}, '-' specifies null
     */
    public String createMsg(String type) {

        String RoutingMsg = "";

        RoutingMsg += "{";
        RoutingMsg += type + " , ";
        RoutingMsg += getIp_addr() + " , ";
        RoutingMsg += String.valueOf(getPort()) + " , ";
        RoutingMsg += String.format("%.1f", getCost_weight()) + " , ";
        RoutingMsg += String.valueOf(isNeighbor()) + " , ";
        RoutingMsg += getNh_ipaddr() + " , ";//info about next hop
        RoutingMsg += String.valueOf(getNh_port()) + " , ";//info about next hop
        RoutingMsg += "end}";

        return RoutingMsg;
    }
    //{ type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end}

    public String createSND(String type) {

        /*
         * add self info to the message
         * Note: for now we assume that, sender send its cost
         *          (same for all neighbors for now and is 1) to the neighbors
         *         and it gets used when the sender is joining the net
         */
        boolean recent = isNeighbor();
        setIsneighbor(true);
        String RoutingMsg = createMsg(type);
        setIsneighbor(recent);

        String nhname;
        String[] nhname_tmp;

        Enumeration<String> in_keys = getDvector().keys();
        String in_name;
        double cst = 0;
        String[] ip_port;

        while (in_keys.hasMoreElements()) {
            in_name = in_keys.nextElement();
            if (!in_name.equals(myName())) {

                String tmpp = getDvector().get(in_name);
                if (tmpp != null) {
                    nhname_tmp = tmpp.split("::");
                    cst = Double.valueOf(nhname_tmp[0].trim()); //costSent
                    nhname = nhname_tmp[1].trim();
                    Node_data nd = meNode.getrTable().get(nhname);

                    if (nd != null) {
                        if (nd.isLinkOn() && nd.isNeighbor() && (nd.getCost_weight() < INF)) {//need to know that the nh is reachable
                            ip_port = in_name.split(":");
                            RoutingMsg += "::{" + type + ", " + ip_port[0] + ", " + ip_port[1] + ", "
                                    + String.format("%.1f", cst) + ", false, " + getNh_ipaddr() + ", "
                                    + String.valueOf(getNh_port()) + ", end}";
                        }
                    }
                }
            }
        }

        return RoutingMsg;
    }

    /**
     * @return the ip_address
     */
    public String getIp_addr() {
        return ip_addr;
    }

    /**
     * @param ip_addr the ip_address to set
     */
    public void setIp_addr(String ip_addr) {
        this.ip_addr = ip_addr;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the cost_weight
     */
    public double getCost_weight() {
        return cost_weight;
    }

    /**
     * @param cost_weight the cost_weight to set
     */
    public void setCost_weight(double cost_weight) {
        this.cost_weight = cost_weight;
    }

    /**
     * @return the isneighbor
     */
    public boolean isNeighbor() {
        return this.isneighbor;
    }

    /**
     * @param isneighbor the isneighbor to set
     */
    public void setIsneighbor(boolean isneighbor) {
        this.isneighbor = isneighbor;
    }

    /**
     * LFC_isAlive make sue the the link failure checker thread is not null
     *
     * @return
     */
    public boolean LFC_isAlive() {
        return getLFC() != null;
    }

    /**
     * this in charge of passing the thread responsible link timeout and
     * disconnection
     *
     * @return Thread LFC, the Link failure Checker
     */
    public LFC_thread getLFC() {
        return LFC;
    }

    /**
     * initialize the link failure checker thread and then start it Note: we
     * assume the input thread has not been started
     *
     * @param LFC the LinkFailureChecker to this Node_data
     */
    public void setLFC(LFC_thread LFC) {
        this.LFC = LFC;
        if (this.LFC != null) {
            this.LFC.start();
        }
    }

    /**
     * @return the nh_ipaddr
     */
    public String getNh_ipaddr() {
        return nh_ipaddr;
    }

    /**
     * @param nh_ipaddr the nh_ipaddr to set
     */
    public void setNh_ipaddr(String nh_ipaddr) {
        this.nh_ipaddr = nh_ipaddr;
    }

    /**
     * @return the nh_port
     */
    public int getNh_port() {
        return nh_port;
    }

    /**
     * @param nh_port the nh_port to set
     */
    public void setNh_port(int nh_port) {
        this.nh_port = nh_port;
    }

    /**
     * Specifies if the node whos data is "this" is connected
     *
     * @return boolean linkOn: true if connected/reachable, false otherwise
     */
    public boolean isLinkOn() {
        return this.linkOn;
    }

    /**
     * turn link on or off (off means it is not part of the routing table)
     *
     * @param linkOn the boolean value specifying the state of the node
     */
    public void setLinkOn(boolean linkOn) {
        this.linkOn = linkOn;
    }

    /**
     * @return the dvector
     */
    public Hashtable<String, String> getDvector() {
        return dvector;
    }

    /**
     * input must be of the form
     *
     * @param dvector the dvector to set
     */
    public void setDvector(Hashtable<String, String> dvector) {
        this.dvector = dvector;
    }
}
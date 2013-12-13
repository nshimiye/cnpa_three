package routing;

import java.util.*;

/**
 * Marcelin Nshimiyimana an object represetation of data used by the distance
 * vector just data collected and saved in the routing table
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
    private boolean linkOn, isneighbor;
    private Thread LFC; //link failure checker
    
    //let's add the src where this data is coming from
    private String nh_ipaddr = "-"; //default
    private int nh_port = 0; //default

    public Node_data(String ip_address, int node_port, double cost, boolean neighbor, Thread LFC_used) {
        ip_addr = ip_address;
        port = node_port;
        cost_weight = cost;
        isneighbor = neighbor;
    }
    
    /**
     * 
     * @return String node_name, in the form ip_addr:port
     */
    public String myName(){
        String node_name = getIp_addr() + ":"+ String.valueOf(getPort());
        return node_name;
    }
    
    /**
     * getDventry 
     * short for distance vector entry, this is very helpful when you
     * need to display routing info as described in the homework
     * @return String <destination, cost> tuple for this(Node_data) data object
     */
    public String getDventry(){
        String dventry = ""; //entry <> for this linked node
        dventry += "< " + getIp_addr() + ", " + String.valueOf(getCost_weight()) +">";
                
        return dventry;
    }

    /**
     * format used is similar to json, except
     *
     * @param type String (ROUTE UPDATE, INIT, SELF) specifies the type of message created (not important for now)
     * @return the message in the format { type, ip_addr, port, cost, isNeighbor, nh_addr, nh_port, end}, '-' specifies null
     */
    public String createMsg(String type) {

        String RoutingMsg = "";

        RoutingMsg += "{";
        RoutingMsg += type + " , ";
        RoutingMsg += getIp_addr() + " , ";
        RoutingMsg += String.valueOf(getPort()) + " , ";
        RoutingMsg += String.valueOf(getCost_weight()) + " , ";
        RoutingMsg += String.valueOf(isNeighbor()) + " , ";
        RoutingMsg += getNh_ipaddr() + " , ";//info about next hop
        RoutingMsg += String.valueOf(getNh_port()) + " , ";//info about next hop
        RoutingMsg += "end }";

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
        return isneighbor;
    }

    /**
     * @param isneighbor the isneighbor to set
     */
    public void setIsneighbor(boolean isneighbor) {
        this.isneighbor = isneighbor;
    }

    /**
     * LFC_isAlive
     * make sue the the link failure checker thread is not null
     * @return 
     */
    public boolean LFC_isAlive() {
        return getLFC() != null;
    }

    /**
     * this in charge of passing the thread responsible link timeout and disconnection 
     * @return Thread LFC, the Link failure Checker
     */
    public Thread getLFC() {
        return LFC;
    }

    /**
     * initialize the link failure checker thread
     * @param LFC the LinkFailureChecker to this Node_data
     */
    public void setLFC(Thread LFC) {
        this.LFC = LFC;
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
     * @return boolean linkOn: true if connected/reachable, false otherwise
     */
    public boolean isLinkOn() {
        return linkOn;
    }

    /**
     * turn link on or off (off means it is not part of the routing table)
     * @param linkOn the boolean value specifying the state of the node 
     */
    public void setLinkOn(boolean linkOn) {
        this.linkOn = linkOn;
    }
}
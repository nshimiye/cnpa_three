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
    private int cost_weight;
    private boolean isneighbor;
    private Thread LFC; //link failure checker

    public Node_data(String ip_address, int node_port, int cost, boolean neighbor, Thread LFC_used) {
        ip_addr = ip_address;
        port = node_port;
        cost_weight = cost;
        isneighbor = neighbor;
    }

    /**
     * format used is similar to json, except
     *
     * @param type (ROUTE UPDATE)
     * @return the message in the format { type, ip_addr, port, cost,
     * isNeighbor}
     */
    public String createMsg(String type) {

        String RoutingMsg = "";

        RoutingMsg += "{";
        RoutingMsg += type + " , ";
        RoutingMsg += getIp_addr() + " , ";
        RoutingMsg += String.valueOf(getPort()) + " , ";
        RoutingMsg += String.valueOf(getCost_weight()) + " , ";
        RoutingMsg += String.valueOf(isNeighbor()) + " , ";
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
    public int getCost_weight() {
        return cost_weight;
    }

    /**
     * @param cost_weight the cost_weight to set
     */
    public void setCost_weight(int cost_weight) {
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

    //make sue the the link failure chevker is not null
    public boolean LFC_isAlive() {
        return getLFC() != null;
    }

    /**
     * @return the LFC
     */
    public Thread getLFC() {
        return LFC;
    }

    /**
     * @param LFC the LFC to set
     */
    public void setLFC(Thread LFC) {
        this.LFC = LFC;
    }
}
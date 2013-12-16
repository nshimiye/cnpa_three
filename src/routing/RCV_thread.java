/*
 * RCV_thread.java
 *
 * Created on __Dec 12, 2013__, __11:36:51 PM__
 *
 * Copyright(c) {2013} Marcellin.  All Rights Reserved.
 * @author Marcellin Nshimiyimana<nshimiye@ovi.com>
 */
package routing;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Queue;

/**
 *
 * @author mars
 */
public class RCV_thread extends Thread {

    /*
     * 1. Given a Client caller
     * 2. we wait for data to come, and then 
     * 3. create the sender's name usin ip and 
     * 4. call the reInit from the client.
     * 5.
     * 
     */
    private Client meNode = null;
    private DatagramSocket socket = null;
    private int owner_port = 0;
    private byte[] buf = null;
    private final int MAX_UDP = 1024;
    private boolean debug = false;
    private DatagramPacket packet = null;
    private boolean stop = false;
    
    private final double INF = 500;

    /**
     * create RCV_thread object
     *
     * @param me_node Client :the client node that we are going to receive
     * message for, it is generally the caller of this thread
     * @param port int :port to listen to for incoming routing messages(udp
     * packets)
     */
    public RCV_thread(Client me_node, int port) {
        meNode = me_node;
        owner_port = port;
        try {
            socket = new DatagramSocket(owner_port);
        } catch (SocketException ex) {
            System.err.printf("\n[RCV_thread]: Error creating udp socket\n");
            //then kill the system
            System.err.printf("[CMD_manager]: Client exiting ...\n");
            System.exit(-1);

        }

    }

    @Override
    public void run() {

        String head_name;
        //1. Get the routing table from the client
        Hashtable<String, Node_data> cmd_rTable;
        Node_data ndt;

        while (true) { //this stops only when client caller exits
            if (debug) {
                System.err.printf("[RCV_thread]: receiving ...\n");
            }
            if (stop) {
                break;
            }
            try {

                //wiat for upd packet
                buf = new byte[MAX_UDP];
                packet = new DatagramPacket(buf, buf.length);
                getSocket().receive(packet);

                String message = new String(packet.getData());
                //message is of format: [{}::{}::{}...] or [lINKDOWN/LINKUP::<node_name>]
                if (debug) {
                    System.err.printf("[RCV_thread]: rcved msg=%s\n", message);
                }

                //parse the packet data and create the sender's name
                String message_tmp = message;
                message_tmp = message_tmp.replace('[', ' ');
                message_tmp = message_tmp.replace(']', ' ');

                String[] attempt_link = message_tmp.split("::"); //meaning assume we have LINKUP/DOWN

                String head = attempt_link[0]; //{ROUTING, ip_addr, port, cost, true, nhAddr, nhPport, end}


                if (head.trim().equals("LINKUP")) {
                    head_name = attempt_link[1];
                    //1. Get the routing table from the client
                    cmd_rTable = meNode.getrTable();
                    ndt = cmd_rTable.get(head_name);

                    if (ndt != null) {
                        //2. Set this node to offline (:linkon=false)
                        ndt.setLinkOn(true);
                        ndt.setIsneighbor(true);
                        LFC_thread lfc = new LFC_thread(meNode, ndt, meNode.getTimer());
                        ndt.setLFC(lfc);

                        System.out.printf("waiking link to <%s>\n", ndt.myName());
                        /*
                         * enable the "alow_send" only
                         * and not worry about the locking system
                         */
                        meNode.setAllow_send(true);
                        //meNode.setRouting_msg("LINKDOWN");
                    }
                } else if (head.trim().equals("LINKDOWN")) {
                    //we just clear or enable the link
                    //here possible race condition with the RCV_thread
                    head_name = attempt_link[1];
                    //1. Get the routing table from the client
                    cmd_rTable = meNode.getrTable();
                    ndt = cmd_rTable.get(head_name);

                    if (ndt != null) {
                        //2. Set this node to offline (:linkon=false)
                        ndt.setLinkOn(false);
                        ndt.setIsneighbor(false);
                        //ndt.setCost_weight(INF);
                        if (ndt.LFC_isAlive()) {
                            ndt.getLFC().setStop(true);
                            ndt.getLFC().interrupt();
                            ndt.setLFC(null);
                        }

                        System.out.printf("clearing link to <%s>\n", ndt.myName());
                        /*
                         * enable the "alow_send" only
                         * and not worry about the locking system
                         */
                        meNode.setAllow_send(true);
                        //meNode.setRouting_msg("LINKDOWN");
                    }

                } else {


                    //String head = message_tmp.split("::")[0]; //{ROUTE UPDATE, ip_addr, port, cost, true, nhAddr, nhPport, end}
                    head = head.replace('{', ' ');
                    head = head.replace('}', ' ');

                    String[] head_tmp = head.trim().split(",");
                    head_name = head_tmp[1].trim() + ":" + head_tmp[2].trim();
                    if (debug) {
                        System.err.printf("[RCV_thread]: rcved[%s] msg=[%s]\n", head_name, message);
                    }
                    boolean status = meNode.reInit(head_name, message);
                    if (!status) {//if reInit exec reports false, then it means no uppdate has been made
                        System.out.printf("[RCV_thread]: reInit failed, no table update made \n");
                    }

                    if (debug) {
                        System.err.printf("[RCV_thread]: rcved msg=[%s]\n", message);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                //System.err.printf("\n[RCV_thread]: Error receiving this data \n ==%s\n", ex.toString());
            }
        }
        //getSocket().close();

    }

    /**
     * @param stop the stop to set
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    /**
     * @return the socket
     */
    public DatagramSocket getSocket() {
        return socket;
    }
}

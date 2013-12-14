/*
 * SND_thread.java
 *
 * Created on __Dec 12, 2013__, __11:44:24 PM__
 *
 * Copyright(c) {2013} Marcellin.  All Rights Reserved.
 * @author Marcellin Nshimiyimana<nshimiye@ovi.com>
 */
package routing;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * send the routing update messages
 *
 * @author mars sending thread
 */
public class SND_thread extends Thread {

    /*
     * 1. get message to be sent, the client(message owner), and neighbors' addresses
     * 2. create udp packet and send it to every neighbor
     * 3.
     * 4
     */
    private Client selfNode = null;
    private String message = null;
    private String[] node_ns = null;
    private long timeout = 3000;
    protected DatagramSocket socket = null;
    
    private final int MAX_UDP = 1024;

    private boolean debug = false;
    /**
     *
     * @param me_node Client :the client/host node, this client should own this
     * SND_thread
     * @param ngb_addrs String : short for neighbor addresses,it is an array of
     * node_names(ip_addr:port)
     * @param time_out Long : period of time that this thread will wait for
     * before it can send the updates
     */
    public SND_thread(Client me_node, String ngb_addrs, long time_out) {

        //need to find a way to stop it when wrong input is found??????????
        selfNode = me_node;
        node_ns = ngb_addrs.split(",");
        timeout = time_out;
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            System.err.printf("\n[SND_thread]: Error creating udp socket\n");
        }

    }

    @Override
    public void run() {

        while (true) { //this stops only when client caller exits

            try {
                for (int i = 0; i < node_ns.length; i++) {
                    String ngb_name = node_ns[i]; //ip_addr:port
                    if (debug) {
                        System.err.printf("<%d>-nd_name=[%s]\n", i, ngb_name);
                    }
                    
                    message = selfNode.rTableForSND(ngb_name);
                    
                    String[] ngb_name_tmp = ngb_name.split(":"); //split ip_addr:port
                    String ip_addr = ngb_name_tmp[0]; // ip_addr
                    int port = Integer.valueOf(ngb_name_tmp[1]);// port

                    //create udp packet and then send it
                    DatagramPacket packet;

                    try {
                        InetAddress Inet = InetAddress.getByName(ip_addr);
                        if (debug) {
                            System.err.printf("[==SND_thread==]:- = [%s]\n", Inet.toString());
                        }
                        packet = new DatagramPacket(message.getBytes(), message.getBytes().length, Inet, port);
                        socket.send(packet);
                        
                       if (true) System.out.printf("[SND_thread-%d]:sending route update to [%s]\n", i,
                               packet.getAddress().getHostAddress()); //to be fixed?????????

                        if (debug) {
                            System.err.printf("[SND_thread]:-just send it to = [%s]\n", packet.getAddress().getHostAddress());
                        }
                    } catch (Exception ex) {
                        System.err.printf("\n[SND_thread]: Error sending this data \n ==%s\n", ex.toString());
                    }
                }

                Thread.sleep(timeout); //wait for timeout and then send again

                //need a try catch
            } catch (InterruptedException ex) {
                //analyse the cause of interrupt
                //if from the updatedv method then send again
            }
        }
    }

    /**
     * @param node_ns String : short for neighbor addresses,it is an array of
     * node_names(ip_addr:port)
     */
    public void setNode_ns(String pnode_ns) {

        this.node_ns = pnode_ns.split(",");

    }
}

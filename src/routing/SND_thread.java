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
import java.util.LinkedList;
import java.util.Queue;

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
    private DatagramSocket socket = null;
    private boolean stop = false;
    private final int MAX_UDP = 1024;
    private boolean debug = false;
    private Queue<String[]> msg_queue = new LinkedList<>();
    private boolean single_snd = false;

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

            //then kill the system
            System.err.printf("[CMD_manager]: Client exiting ...\n");
            System.exit(-1);
        }

    }

    @Override
    public void run() {
//create udp packet and then send it
        DatagramPacket packet;
        String[] ngb_name_tmp; //split ip_addr:port
        String ip_addr; // ip_addr
        int port;// port

        while (true) { //this stops only when client caller exits
            if (stop) {
                break;
            }

            try {

                if (single_snd) { //linkdown and linkup
                    while (!msg_queue.isEmpty()) {
                        //format: [0] = LINKDOWN, LINKUP, [ROUTING] 
                        //        [1] = node_name or [all]
                        String[] msg_snd = getMsg_queue().poll();
                        String type = msg_snd[0];
                        String nd_name = msg_snd[1]; //assume this is always 2

                        String tobesent = "[" + type.trim() + "::" + selfNode.get_myData().myName().trim() + "]";

                        ngb_name_tmp = nd_name.split(":"); //split ip_addr:port
                        ip_addr = ngb_name_tmp[0].trim(); // ip_addr
                        port = Integer.valueOf(ngb_name_tmp[1].trim());// port

                        try {
                            InetAddress Inet = InetAddress.getByName(ip_addr);
                            if (debug) {
                                System.err.printf("[==SND_thread==]:- = [%s]\n", Inet.toString());
                            }
                            packet = new DatagramPacket(tobesent.getBytes(), tobesent.getBytes().length, Inet, port);
                            getSocket().send(packet);


                        } catch (Exception ex) {
                            System.err.printf("\n[SND_thread]: Error sending this data \n ==%s\n", ex.toString());
                        }
                    }
                    single_snd = false;

                } else {
                    for (int i = 0; i < node_ns.length; i++) {
                        String ngb_name = node_ns[i]; //ip_addr:port
                        if (debug) {
                            System.err.printf("<%d>-nd_name=[%s]\n", i, ngb_name);
                        }
                        Node_data ntmp = selfNode.getrTable().get(ngb_name);
                        if (ntmp != null) {//then we know we have valid nodename
                            message = selfNode.rTableForSND(ngb_name);

                            ngb_name_tmp = ngb_name.split(":"); //split ip_addr:port
                            ip_addr = ngb_name_tmp[0].trim(); // ip_addr
                            port = Integer.valueOf(ngb_name_tmp[1].trim());// port


                            try {
                                InetAddress Inet = InetAddress.getByName(ip_addr);
                                if (debug) {
                                    System.err.printf("[==SND_thread==]:- = [%s]\n", Inet.toString());
                                }
                                packet = new DatagramPacket(message.getBytes(), message.getBytes().length, Inet, port);
                                getSocket().send(packet);

                                if (debug) {
                                    System.out.printf("[SND_thread-%d]:sending route update to [%s]\n", i,
                                            packet.getAddress().getHostAddress()); //to be fixed?????????
                                }
                                if (debug) {
                                    System.err.printf("[SND_thread]:-just send it to = [%s]\n", packet.getAddress().getHostAddress());
                                }
                            } catch (Exception ex) {
                                System.err.printf("\n[SND_thread]: Error sending this data \n ==%s\n", ex.toString());
                            }
                        }
                    }
                }
                Thread.sleep(timeout); //wait for timeout and then send again

                //need a try catch
            } catch (InterruptedException ex) {
                //analyse the cause of interrupt
                //if from the updatedv method then send again

                if (stop) {
                    break;
                }
            }
        }
        //getSocket().close();

    }

    /**
     * @param node_ns String : short for neighbor addresses,it is an array of
     * node_names(ip_addr:port)
     */
    public void setNode_ns(String pnode_ns) {

        this.node_ns = pnode_ns.split(",");

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

    /**
     * @return the msg_queue
     */
    public Queue<String[]> getMsg_queue() {
        return msg_queue;
    }

    /**
     * @param single_snd the single_snd to set
     */
    public void setSingle_snd(boolean single_snd) {
        this.single_snd = single_snd;
    }
}

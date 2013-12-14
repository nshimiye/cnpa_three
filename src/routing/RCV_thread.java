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
    DatagramPacket packet = null;

    /**
     * create RCV_thread object
     * @param me_node Client :the client node that we are going to receive message for,
     *          it is generally the caller of this thread
     * @param port int :port to listen to for incoming routing messages(udp packets) 
     */
    public RCV_thread(Client me_node, int port) {
        meNode = me_node;
        owner_port = port;
        try {
            socket = new DatagramSocket(owner_port);
        } catch (SocketException ex) {
            System.err.printf("\n[RCV_thread]: Error creating udp socket\n");
        }

    }

    @Override
    public void run() {

        while (true) { //this stops only when client caller exits

            try {

                //wiat for upd packet
                buf = new byte[MAX_UDP];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String message = new String(packet.getData());
                //message is of format: [{}::{}::{}...]
                if (debug) {
                    System.err.printf("[RCV_thread]: rcved msg=[%s]\n", message);
                }

                //parse the packet data and create the sender's name
                String message_tmp = message;
                message_tmp = message_tmp.replace('[', ' ');
                String head = message_tmp.split("::")[0]; //{ROUTE UPDATE, ip_addr, port, cost, true, nhAddr, nhPport, end}
                String[] head_tmp = head.trim().split(",");
                String head_name = head_tmp[1].trim() + ":" + head_tmp[2].trim();

                boolean status = meNode.reInit(head_name, message);
                if (!status) {//if reInit exec reports false, then it means no uppdate has been made
                    System.out.printf("[RCV_thread]: reInit failed, no table update made \n");
                }

                if (debug) {
                    System.err.printf("[RCV_thread]: rcved msg=[%s]\n", message);
                }

            } catch (Exception ex) {
                System.err.printf("\n[RCV_thread]: Error receiving this data \n ==%s\n", ex.toString());
            }
        }

    }
}

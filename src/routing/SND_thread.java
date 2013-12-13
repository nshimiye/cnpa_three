/*
 * SND_thread.java
 *
 * Created on __Dec 12, 2013__, __11:44:24 PM__
 *
 * Copyright(c) {2013} Marcellin.  All Rights Reserved.
 * @author Marcellin Nshimiyimana<nshimiye@ovi.com>
 */
package routing;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private String message = null;
    private String[] node_ns = null;
    private long timeout = 300;

    /**
     *
     * @param me_node Client :the client/host node, this client should own this
     * SND_thread
     * @param ngb_addrs String : short for neighbor addresses,it is an array of
     * node_names(ip_addr:port)
     */
    public SND_thread(Client me_node, String ngb_addrs) {

        //need to find a way to stop it when wrong input is found??????????

        message = me_node.rTableForSND();
        node_ns = ngb_addrs.split(",");

    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < node_ns.length; i++) {

                String ngb_name = node_ns[i]; //ip_addr:port
                System.err.printf("<%d>-nd_name=[%s]\n", i, ngb_name);
                String[] ngb_name_tmp = ngb_name.split(":"); //split ip_addr:port
                String ip_addr = ngb_name_tmp[0]; // ip_addr
                int port = Integer.valueOf(ngb_name_tmp[1]);// port

                //create udp packet and then send it

                //i am here??????????????
            }

            Thread.sleep(timeout); //wait for timeout and then send again

            //need a try catch
        } catch (InterruptedException ex) {
            //analyse the cause of interrupt
            //if from the updatedv method then send again
        }

    }
}

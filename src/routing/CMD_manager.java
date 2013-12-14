/*
 * CMD_manager.java
 *
 * Created on __Dec 12, 2013__, __11:46:09 PM__
 *
 * Copyright(c) {2013} Marcellin.  All Rights Reserved.
 * @author Marcellin Nshimiyimana<nshimiye@ovi.com>
 */
package routing;

import java.util.Hashtable;
import java.util.Scanner;

/**
 *
 * @author mars command manager
 */
public class CMD_manager extends Thread {

    /*
     * 1. given a client caller
     * 2. wait for input from the terminal
     * 3. check for correctness of the command
     * 4. get the requested info (from the client who owns this cmd manager)
     * 5.
     * ...
     */
    /**
     * Description from hw about commands
     *
     * LINKDOWN {ip_address port} – This allows the user to destroy an existing
     * link i.e. change the link cost to infinity to the mentioned neighbor.
     *
     * • LINKUP {ip_address port}– This allows the user to restore the link to
     * the mentioned neighbor to the original value after it was destroyed by a
     * LINKDOWN.
     *
     * • SHOWRT – This allows the user to view the current routing table of the
     * client. ie. It should indicate for each other client in the network, the
     * cost and neighbor used to reach that client.
     *
     * • CLOSE – With this command the client process should close/shutdown.
     * Link failures is also assumed when a client doesn’t receive a ROUTE
     * UPDATE message from a neighbor (i.e., hasn’t ‘heard’ from a neighbor) for
     * 3*TIMEOUT seconds.
     *
     * This happens when the neighbor client crashes or if the user calls the
     * CLOSE command for it. When this happens, the link cost should be set to
     * infinity and the client should stop sending ROUTE UPDATE messages to that
     * neighbor. The link is assumed to be dead until the process comes up and a
     * ROUTE UPDATE message is received again from that neighbor
     */
    private Client meNode = null;

    public CMD_manager(Client me_node) {
        meNode = me_node;

    }

    @Override
    public void run() {

        //create/open input stream
        Scanner inp = new Scanner(System.in);
        String[] cmd;
        String cmd_tmp = "";
        while (true) { //this stops only when client caller exits

            //wait for input command
            cmd_tmp = inp.next();
            cmd = cmd_tmp.trim().split(" ");
            //processing together with outputting results
            if (cmd.length >= 1) {
                switch (cmd[0]) {

                    //SHOWRT –
                    case "SHOWRT":
                        System.out.printf("<Current Time>Distance vector list is:\n%s\n", meNode.tableToString());

                        break;

                    // LINKDOWN {ip_address port} –
                    case "LINKDOWN":

                        if (cmd.length != 3) {
                            System.err.printf("[CMD_manager]: wrong use of the command ...\n");
                        } else { //here we start the linkdown operation

                            //here possible race condition with the RCV_thread

                            String head_name = cmd[1].trim() + ":" + cmd[2].trim();
                            //1. Get the routing table from the client
                            Hashtable<String, Node_data> cmd_rTable = meNode.getrTable();
                            Node_data ndt = cmd_rTable.get(head_name);

                            if (ndt != null) {
                                //2. Set this node to offline (:linkon=false)
                                ndt.setLinkOn(false);
                                //ndt.setCost_weight(500); //this is infinity

                                /*
                                 * 3.tell the client to send LINK DWON update
                                 * this is not necessary if RCV_thread is processing
                                 * msg from other nodes, so
                                 * we can add a sensing system that will tell CMD_manager
                                 * to just enable "alow_send" of the client if RCV_thread
                                 * is processing. (: help avoiding race condition)
                                 * 
                                 * we use a locking system
                                 */

                                /*
                                 * we are actually going to enable the "alow_send" only
                                 * and not worry about the locking system
                                 */
                                meNode.setAllow_send(true);
                                meNode.setRouting_msg("LINKDOWN");
                                //  boolean status = meNode.reInit(head_name, message);
                            }
                        }
                        break;

                    //LINKUP {ip_address port}–
                    case "LINKUP":
                        if (cmd.length != 3) {
                            System.err.printf("[CMD_manager]: wrong use of the command ...\n");
                        } else { //here we start the linkdown operation

                            //here possible race condition with the RCV_thread

                            String head_name = cmd[1].trim() + ":" + cmd[2].trim();
                            //1. Get the routing table from the client
                            Hashtable<String, Node_data> cmd_rTable = meNode.getrTable();
                            Node_data ndt = cmd_rTable.get(head_name);

                            if (ndt != null) {
                                //2. Set this node to offline (:linkon=false)
                                ndt.setLinkOn(false);

                                /*
                                 * enable the "alow_send" only
                                 * and not worry about the locking system
                                 */
                                meNode.setAllow_send(true);
                                meNode.setRouting_msg("LINKDOWN");
                            }


                        }
                        break;

                    //CLOSE –
                    case "CLOSE":

                        //close all open socket 

                        //then kill the system
                        System.out.printf("[CMD_manager]: Client exiting ...\n");
                        //i am here?????????????

                        System.exit(0);

                        break;

                    default:
                        System.err.printf("[CMD_manager]: command not implemented ...\n");
                        break;

                }
            } else {
                System.err.printf("[CMD_manager]: unknown command ...\n");

            }


            try {
            } catch (Exception ex) {
            }


        }
    }
}

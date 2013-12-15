/*
 * CMD_manager.java
 *
 * Created on __Dec 12, 2013__, __11:46:09 PM__
 *
 * Copyright(c) {2013} Marcellin.  All Rights Reserved.
 * @author Marcellin Nshimiyimana<nshimiye@ovi.com>
 */
package routing;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Scanner;

/**
 * command manager
 *
 * @author mars
 */
public class CMD_manager {

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
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private Date date;

    /**
     *
     * @param myPort int : the listening port, on which neighbor send their
     * routing table info
     * @param send_timer long: the timeout for resending routing table info to
     * neighbors
     * @param node_data String: information about the neighbor of this client
     * node
     */
    public CMD_manager(int myPort, long send_timer, String... node_data) {
        meNode = new Client(myPort, send_timer, node_data);

    }

    /**
     * in charge of processing commands and outputting results
     */
    public void cmd_manage() {

        System.out.printf("Node starting on <%s>", meNode.get_myData().myName());
        System.out.printf("Use these commands to interact with the Node\n"
                + "[SHOWRT, LINKDOWN, LINKUP, CLOSE], or type \"help\" or \"usage\" to see the commands' usage\n\n[CMD_manager]:");

        //create/open input stream
        Scanner inp = new Scanner(System.in);
        String[] cmd;
        String cmd_tmp = "";
        while (true) { //this stops only when client caller exits

            //wait for input command
            cmd_tmp = inp.nextLine();
            if(false){System.out.printf("entry = %s\n", cmd_tmp);}
            cmd = cmd_tmp.trim().split(" ");
            if(false){System.out.printf("command = %s\n", cmd[0]);}
            //processing together with outputting results
            if (cmd.length >= 1) {
                switch (cmd[0].toUpperCase()) {

                    //SHOWRT –
                    case "SHOWRT":
                        date = new Date();
                        System.out.printf("<%s> Distance vector list is:\n%s\n",
                                dateFormat.format(date), meNode.tableToString());
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
                                ndt.setIsneighbor(false);
                                System.out.printf("clearing link to <%s>\n", ndt.myName());
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
                                ndt.setLinkOn(true);
                                ndt.setIsneighbor(true);
                                System.out.printf("waiking link to <%s>\n", ndt.myName());
                                /*
                                 * enable the "alow_send" only
                                 * and not worry about the locking system
                                 */
                                meNode.setAllow_send(true);
                                meNode.setRouting_msg("LINKDOWN");
                            }


                        }
                        break;

                    case "HELP":
                    case "USAGE":
                        System.out.printf(
                                "• LINKDOWN <ip_address port> – This allows the user to destroy an existing link \n"
                                + "i.e. change the link cost to infinity to the mentioned neighbor. 	\n"
                                + " • LINKUP <ip_address port>– This allows the user to restore the link to the \n"
                                + "mentioned neighbor to the original value after it was destroyed by a LINKDOWN. 	\n"
                                + " • SHOWRT – This allows the user to view the current routing table of the client. ie. \n"
                                + "It should indicate for each other client in the network, the cost and neighbor used \n"
                                + "to reach that client. 	\n"
                                + " • CLOSE – With this command the client process should close/shutdown.  \n");
                        break;

                    //CLOSE –
                    case "CLOSE":

                        //close all open socket 
                        meNode.getReceiver().setStop(true);
                        meNode.getReceiver().getSocket().close();
                        meNode.getSender().setStop(true);
                        meNode.getSender().getSocket().close();

                        inp.close();

                        //then kill the system
                        System.out.printf("[CMD_manager]: Client exiting ...\n");

                        System.exit(0);

                        break;

                    default:
                        System.err.printf("[CMD_manager]: command not implemented ...\n");
                        break;

                }
            } else {
                System.err.printf("[CMD_manager]: unknown command ...\n");

            }

            System.out.printf("\n[CMD_manager]:");


        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args_saved) {
        int port = 20005;
        long timer = 5;
//        if(args.length < 2){        
//            System.err.println("usage: java CMD_manager <localport> <timeout> <[ipaddress1 port1 weight1 ...]>\n");
//            System.exit(-1);
//        }

        String[] args = "160.39.193.42 20000 2.1 128.59.196.2 20001 2.2 128.59.196.4 20000 1".split(" ");
        //String[] args = "128.59.196.2 20000 4.1 128.59.196.2 20001 5.2 128.59.196.4 20000 3".split(" ");
        String[] st = null;
        if (args.length % 3 == 0) {
            st = new String[args.length / 3];
            for (int i = 0; i < st.length; i++) {
                st[i] = " " + args[i * 3] + " " + args[(i * 3) + 1] + " " + args[(i * 3) + 2] + " ";
                //System.out.printf("%s\n", st[i]);
            }

        } else {
            System.err.println("possible input error");
            System.exit(-1);
        }
        // testing the client's methods
        // 128.59.196.2 20000 4.1 128.59.196.2 20001 5.2
        CMD_manager node_creator = new CMD_manager(port, timer, st);

        node_creator.cmd_manage();
    }
}

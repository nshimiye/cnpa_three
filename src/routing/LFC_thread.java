

import java.util.Hashtable;

/**
 *
 * @author element
 */
public class LFC_thread extends Thread {

    /*
     * 1. we need the node_data
     * 
     * 2. a delay (in millsec) that specify the timeout
     * 
     * 3. the sender thread that sends ROUTING UPDATE on timeout
     * 
     * 4. the bounded Client node, owner of this node_data object ndt 
     * 
     * Summary: after init/creation, we create a loop  
     */
    private Node_data ndata = null;
    private Client bNode = null;
    private long timer = 3000;
    private boolean stop = false;
    private final double INF = 500;

    /**
     *
     * @param bnode
     * @param ndt
     * @param timeout
     */
    public LFC_thread(Client bnode, Node_data ndt, long timeout) {
        ndata = ndt;
        bNode = bnode;
        timer = timeout;
    }

    @Override
    public void run() {

        while (true) { //this stops only when client caller exits
            if (this.stop) {
                break;
            }

            try {
                Thread.sleep(timer * 3); //wait for awhile and then clear the link
                //to the bounded client node.

                //here possible race condition with the RCV_thread
                String node_name = ndata.myName();
                //1. Get the routing table from the client
                Hashtable<String, Node_data> cmd_rTable = bNode.getrTable();
                Node_data ndt = cmd_rTable.get(node_name);

                if (ndt != null) {
                    //2. Set this node to offline (:linkon=false)
                    //bNode.getrTable().get(ndt.myName()).setLinkOn(false);
                    bNode.getrTable().get(ndt.myName()).setIsneighbor(false);
                    bNode.getrTable().get(ndt.myName()).setCost_weight(INF);
                    bNode.getrTable().get(ndt.myName()).closeLinks();
                    String nhn = bNode.getrTable().get(ndt.myName()).nhName();

                    bNode.getrTable().get(nhn).getDvector().remove(ndt.myName());

//                     System.err.printf("[LFC_thread ]: dying... name= [%s]>%.1f linkon=[%b]\n", ndt.myName(),
//                            bNode.getrTable().get(ndt.myName()).getCost_weight(), bNode.getrTable().get(ndt.myName()).isLinkOn());
                    //bNode.getrTable().remove(ndt.myName());
                    // ndt.setLFC(null);
                    // ndt = null;
//                    //put Linkdown message in queue and interrupt
//                    Queue<String[]> msg_queue = bNode.getSender().getMsg_queue();
//                    String[] msg = new String[2];
//                    msg[0] = "LINKDOWN";
//                    msg[1] = ndt.myName();
//                    msg_queue.add(msg);
//
//                    bNode.getSender().setSingle_snd(true);
                    //bNode.getSender().setNode_ns(bNode.getNeighbors());
                    //bNode.getSender().interrupt();


                    //System.out.printf("clearing link to <%s>\n", ndt.myName());
                    //ndt.setCost_weight(500); //this is infinity
                    break; //this break allows this LFC_thread to exit
                }

            } //need a try catch
            catch (InterruptedException ex) {
                //analyse the cause of interrupt
                //if from the updatedv method then send again

                if (this.stop) {
                    break;
                }
            }
        }
    }

    /**
     * @param stop the stop to set
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }
}

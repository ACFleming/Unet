package SetupAgents
import org.arl.fjage.shell.Services
import org.arl.unet.Services
import SetupAgents.LoadGenerator
import org.arl.unet.UnetAgent

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*

class LinkGenerator extends UnetAgent {

    private List<Integer> dest_nodes                     // list of possible destination nodes
    private float load                                  // normalized load to generate
    private boolean tx_flag
    private AgentID mac
    private AgentID phy
    private AgentID node
    private AgentID uwlink
    
    def data_msg = PDU.withFormat
    {
        uint32('data')
    }

    LinkGenerator(List<Integer> dn, float l, boolean tx) {
        this.dest_nodes = dn                        
        this.load = l
        this.tx_flag = tx
        print "TX: ${tx_flag}\n"               
    }

    @Override
    void startup() {
        phy = agentForService Services.PHYSICAL
        mac = agentForService Services.MAC
        node = agentForService Services.NODE_INFO
        uwlink = agentForService Services.DATAGRAM
        
        float dataPktDuration = get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
        println "Link Generator : dataPktDuration = ${dataPktDuration}"
        float rate = load/dataPktDuration                 
        // compute average packet arrival rate
        if(this.tx_flag == true){
            print "SENDER\n"
            add new PoissonBehavior((int)(1000/rate), {              
                // create Poisson arrivals at given rate
                // print "${node.address} ${dest_nodes}\n"
                def target = rnditem(dest_nodes)
                // target = dest_nodes[0]
                uwlink << new DatagramReq( to: target, protocol: Protocol.DATA, data : data_msg.encode([ data : 25]))
            })
        }else{
            // print "NOT SENDER\"
        }

        


    }

    @Override
    void processMessage(Message msg) {
        if(msg == null){
            print "${msg}\n"
        }
        

    }


}

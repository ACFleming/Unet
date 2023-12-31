package SetupAgents
import org.arl.fjage.shell.Services
import org.arl.unet.Services

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*

class LoadGenerator extends UnetAgent {

    private List<Integer> dest_nodes                     // list of possible destination nodes
    private float load                                  // normalized load to generate
    private boolean tx_flag
    private AgentID mac
    private AgentID phy
    private AgentID node
    
    def data_msg = PDU.withFormat
    {
        uint32('data')
    }

    LoadGenerator(List<Integer> dest_nodes, float load, boolean tx_flag) {
        this.dest_nodes = dest_nodes                        
        this.load = load
        this.tx_flag = tx_flag
        print "TX: ${tx_flag}\n"                                 
    }

    void setDestNodes(List<Integer> d){
        this.dest_nodes = d
    }

    void setLoad(float l){
        this.load = l
    }


    void setTxFlag(boolean tx){
        this.tx_flag = tx
    }

    @Override
    void startup() {
        phy = agentForService Services.PHYSICAL
        mac = agentForService Services.MAC
        node = agentForService Services.NODE_INFO
        
        float dataPktDuration = get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
        println "Load Generator : dataPktDuration = ${dataPktDuration}"
        float rate = load/dataPktDuration                 
        // compute average packet arrival rate
        if(this.tx_flag){
            add new PoissonBehavior((int)(1000/rate), {              
                // create Poisson arrivals at given rate
                // print "${node.address} ${dest_nodes}\n"
                def target = rnditem(dest_nodes)
                // target = dest_nodes[0]
                // print "Sending to ${target}\n"
                mac << new ReservationReq(to: target, duration: dataPktDuration)
            })
        }

        


    }

    @Override
    void processMessage(Message msg) {
        if (msg instanceof ReservationStatusNtf && msg.status == ReservationStatus.START) {
            phy << new ClearReq()                                   // stop any ongoing transmission or reception
            phy << new TxFrameReq(to: msg.to, type: Physical.DATA , data : data_msg.encode([ data : 25]), protocol : Protocol.USER )             

        }
    }

}

package SetupAgents
import org.arl.fjage.shell.Services
import org.arl.unet.Services

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*

class LoadGenerator extends UnetAgent {

    private List<Integer> destNodes                     // list of possible destination nodes
    private float load                                  // normalized load to generate
    private AgentID mac
    private AgentID phy
    private AgentID node
    
    def data_msg = PDU.withFormat
    {
        uint32('data')
    }

    LoadGenerator(List<Integer> destNodes, float load) {
        this.destNodes = destNodes                        
        this.load = load                                  
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
        add new PoissonBehavior((int)(1000/rate), {              
            // create Poisson arrivals at given rate
            // print "${node.address} ${destNodes}\n"
            def target = rnditem(destNodes)
            // target = destNodes[0]
            // print "Sending to ${target}\n"
            mac << new ReservationReq(to: target, duration: dataPktDuration)
        })
        


    }

    @Override
    void processMessage(Message msg) {
        if (msg instanceof ReservationStatusNtf && msg.status == ReservationStatus.START) {
            phy << new ClearReq()                                   // stop any ongoing transmission or reception
            phy << new TxFrameReq(to: msg.to, type: Physical.DATA , data : data_msg.encode([ data : 25]), protocol : Protocol.USER )             

        }
    }

}

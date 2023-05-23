package SetupAgents

import org.arl.fjage.shell.Services
import org.arl.unet.Services
import org.arl.unet.DatagramReq
import org.arl.unet.Protocol
import org.arl.fjage.AgentID
import org.arl.fjage.OneShotBehavior
import org.arl.unet.net.GetRouteReq
import org.arl.fjage.WakerBehavior
import SetupAgents.LoadGenerator
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*
import org.arl.unet.net.*
class TransportGenerator extends LoadGenerator {
    
    private List<Integer> destNodes                     // list of possible destination nodes
    private float load                                  // normalized load to generate
    private AgentID mac
    private AgentID phy
    private AgentID uwlink
    private AgentID router
    private AgentID rdp
    private AgentID node
    private boolean tx_flag
    def counter = 0

    TransportGenerator(List<Integer> destNodes, float load) {
        this.destNodes = destNodes                        
        this.load = load
    }


    @Override
    void startup() {
        print "TG Startup\n"
        this.phy = agentForService Services.PHYSICAL
        this.mac = agentForService Services.MAC
        this.uwlink = agentForService Services.DATAGRAM
        this.router = agentForService Services.ROUTING
        this.rdp = agentForService Services.ROUTE_MAINTENANCE
        this.node = agentForService(Services.NODE_INFO)  
        
        // print router
        AgentID transport = agentForService Services.TRANSPORT
        // def node = agentForService Services.NODE_INFO
        
        float dataPktDuration = get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
        println "Transport Generator : dataPktDuration = ${dataPktDuration}"
        float rate = load/dataPktDuration                 

        add new WakerBehavior(1000,{
            print "SENDER @ ${100/rate}\n"
            add new PoissonBehavior((int)(100/rate), {              
                // create Poisson arrivals at given rate
                def target = rnditem(destNodes)
                router << new DatagramReq( to: target, protocol: Protocol.DATA, data : data_msg.encode([ data : 25]))
            })
        })
    }

    @Override
    void processMessage(Message msg) {
        if(msg == null){
            print "${msg}\n"
        }
        

    }


}

import org.arl.fjage.shell.Services
import org.arl.unet.Services
import org.arl.unet.DatagramReq
import org.arl.unet.Protocol
import org.arl.fjage.AgentID
import org.arl.fjage.OneShotBehavior
import org.arl.unet.net.GetRouteReq
import org.arl.fjage.WakerBehavior
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*
import org.arl.unet.net.*
// import org.arl.fjage.TickerBehavior

class TransportGenerator extends UnetAgent {
    
    private List<Integer> addressNodes  
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

    def data_msg = PDU.withFormat
    {
        uint32('data')
    }

    TransportGenerator(List<Integer> addressNodes, List<Integer> destNodes, float load, boolean tx_flag) {
        this.destNodes = destNodes                        
        this.load = load
        this.tx_flag = tx_flag 
        this.addressNodes = addressNodes                           
    }

    void addRoutes(List<List<List<Integer>>> routes){

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

        add new OneShotBehavior({
            // print "ready for mac\n"
            // router << new GetRouteReq(to:9)
            def rsp = router.request(new GetRouteReq(to:9))
            print "${rsp}\n"
            if(this.tx_flag == true){
                print "SENDER @ ${100/rate}\n"
                add new PoissonBehavior((int)(100/rate), {              
                    // create Poisson arrivals at given rate
                    def target = rnditem(destNodes)
                    // target = destNodes[0]
                    print "Sending to ${target} from ${this.node.address}\n"
                    mac << new ReservationReq(to: target, duration: dataPktDuration)
                


                })
            }else{
                print "NOT SENDER\n"
            }
        })


        


    }

    @Override
    void processMessage(Message msg) {
        if (msg instanceof ReservationStatusNtf && msg.status == ReservationStatus.START) {
            print "GOT RESERVATION\n"
            // phy << new DatagramReq(to: msg.to)
            router << new DatagramReq( to: msg.to, protocol: Protocol.DATA, data : data_msg.encode([ data : 25]))
            // counter = counter +1       
            print "SENT\n"

        }else{
            print "${msg}\n"
            // if(msg instanceof )
        }
    }

}

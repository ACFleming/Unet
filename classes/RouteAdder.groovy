import org.arl.fjage.shell.Services
import org.arl.unet.Services
import org.arl.unet.DatagramReq
import org.arl.unet.Protocol
import org.arl.fjage.AgentID
import org.arl.fjage.OneShotBehavior
import org.arl.unet.net.GetRouteReq
import org.arl.fjage.WakerBehavior
import org.arl.unet.net.EditRouteReq
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*
import org.arl.unet.net.*
// import org.arl.fjage.TickerBehavior

class RouteAdder extends UnetAgent {
    
    private List<List<Integer>> routes
    private List<Integer> addresses
    private List<Integer> dists
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

    RouteAdder(List<List<Integer>> routes, List<Integer> addresses, List<Integer> dists) {
        this.routes = routes
        this.addresses = addresses
        this.dists = dists
        print "IN\n"                        
    }


    @Override
    void startup() {
        this.phy = agentForService Services.PHYSICAL
        this.mac = agentForService Services.MAC
        this.uwlink = agentForService Services.DATAGRAM
        this.router = agentForService Services.ROUTING
        this.rdp = agentForService Services.ROUTE_MAINTENANCE
        this.node = agentForService(Services.NODE_INFO)  
        
        add new OneShotBehavior({
            for(int to = 0; to < this.routes.size(); to++){
                print "Routing from ${this.node.address} to ${addresses[to]} via ${this.routes[to]}\n"
                for(int nextHop in this.routes[to]){
                    // print "Routing to ${addresses[to]} via ${nextHop} from ${this.node.address}\n"
                    // print " ${nextHop},"
                    def r = EditRouteReq.newRoute()
                    r.to = addresses[to]
                    r.nextHop = nextHop
                    r.hops = this.dists[to]
                    this.router << r
                }
                // print "\n"
            }
        
        })
            



        


    }



}

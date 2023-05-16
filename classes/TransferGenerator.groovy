import org.arl.fjage.shell.Services
import org.arl.unet.Services
import org.arl.unet.DatagramReq
import org.arl.unet.Protocol
import org.arl.fjage.AgentID
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*
// import org.arl.fjage.TickerBehavior

class TransferGenerator extends UnetAgent {

    private List<Integer> destNodes                     // list of possible destination nodes
    private float load                                  // normalized load to generate
    private AgentID mac, phy, uwlink
    def counter = 0

    def data_msg = PDU.withFormat
    {
        uint32('data')
    }

    TransferGenerator(List<Integer> destNodes, float load) {
        this.destNodes = destNodes                        
        this.load = load                                  
    }

    @Override
    void startup() {
        phy = agentForService Services.PHYSICAL
        mac = agentForService Services.MAC
        uwlink = agentForService Services.DATAGRAM
        AgentID transport = agentForService Services.TRANSPORT
        def node = agentForService Services.NODE_INFO
        
        float dataPktDuration = get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
        println "Transmit Generator : dataPktDuration = ${dataPktDuration}"
        float rate = load/dataPktDuration                 
        // compute average packet arrival rate
        add new PoissonBehavior((int)(1000/rate), {              
            // create Poisson arrivals at given rate
            if(counter > -1){
                def target = rnditem(destNodes)
                mac << new ReservationReq(to: target, duration: dataPktDuration)
            }else{
                print  "Node: ${node} DONE ${counter}\n"
                // System.
                
                
            }


        })
        


    }

    @Override
    void processMessage(Message msg) {
        if (msg instanceof ReservationStatusNtf && msg.status == ReservationStatus.START) {
            print "GOT RESERVATION\n"
            // phy << new ClearReq()                                   // stop any ongoing transmission or reception
            // phy << new TxFrameReq(to: msg.to, type: Physical.DATA , data : data_msg.encode([ data : counter]), protocol : Protocol.USER ) 
            
            uwlink.send(new DatagramReq(to: msg.to, protocol: Protocol.USER, data : data_msg.encode([ data : counter] ))  )  
            // counter = counter +1       
            print "SENT\n"

        }else if (msg instanceof  DatagramDeliveryNtf){
            counter +=1
            // print "${msg}\n"
        }else{
            print "${msg}\n"
        }
    }

}

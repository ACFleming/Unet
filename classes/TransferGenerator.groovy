import org.arl.fjage.shell.Services
import org.arl.unet.Services
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*

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
        def node = agentForService Services.NODE_INFO
        
        float dataPktDuration = get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
        println "Load Generator : dataPktDuration = ${dataPktDuration}"
        float rate = load/dataPktDuration                 
        // compute average packet arrival rate
        add new PoissonBehavior((int)(1000/rate), {              
            // create Poisson arrivals at given rate
            if(counter < 100){
                def target = rnditem(destNodes)
                mac << new ReservationReq(to: target, duration: dataPktDuration)
            }else{
                print "DONE ${counter} Node: ${node.address}\n"
                counter +=1
                
            }


        })
        


    }

    @Override
    void processMessage(Message msg) {
        if (msg instanceof ReservationStatusNtf && msg.status == ReservationStatus.START) {
            
            phy << new ClearReq()                                   // stop any ongoing transmission or reception
            phy << new TxFrameReq(to: msg.to, type: Physical.DATA , data : data_msg.encode([ data : counter++]), protocol : Protocol.USER )             

        }else{
            println msg
        }
    }

}

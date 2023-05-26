package SetupAgents
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*

class OldLoadGenerator extends UnetAgent {

  private List<Integer> destNodes                     // list of possible destination nodes
  private float load                                  // normalized load to generate
  private AgentID mac, phy
  int count = 0

  OldLoadGenerator(List<Integer> destNodes, float load) {
    this.destNodes = destNodes                        
    this.load = load                                  
  }

  def dataMsg = PDU.withFormat
  {
    uint32('data')
  }   

  @Override
  void startup() 
  {
    phy = agentForService Services.PHYSICAL
    mac = agentForService Services.MAC
    def node = agentForService(Services.NODE_INFO)
    float dataPktDuration = get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
    // compute average packet arrival rate 
    float rate = load/dataPktDuration                 
    println "dataPktDuration = ${dataPktDuration} ${1000/rate}"
    // create Poisson arrivals at given rate
    add new PoissonBehavior((int)(1000/rate),   
    {
        def req = new ReservationReq(to: rnditem(destNodes), duration: dataPktDuration) 
        mac << req
    })  
  }

  @Override
  void processMessage(Message msg) 
  {
    if (msg instanceof ReservationStatusNtf && msg.status == ReservationStatus.START) 
    {
      phy << new ClearReq()                                   // stop any ongoing transmission or reception
      phy << new TxFrameReq(to: msg.to, type: Physical.DATA, protocol : Protocol.USER, data : dataMsg.encode([data : 25]))  // start a new transmission
    }
  }

}

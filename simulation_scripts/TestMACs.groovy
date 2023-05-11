import org.apache.commons.lang3.RandomUtils
// import org.arl.unet.mac.CSMA
// import org.arl.unet.addr.AddressResolution
//! Simulation
import org.arl.fjage.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import org.arl.unet.api.*
import org.apache.commons.math3.random.*
// import org.arl.unet.api.UnetSocket


///////////////////////////////////////////////////////////////////////////////
// simulation settings

// platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel and modem settings




// modem = [
//   model:            org.arl.unet.sim.HalfDuplexModem,
//   dataRate:         [800.bps, 2400.bps],
//   frameLength:      [16.bytes, 64.bytes],
//   powerLevel:       [0.dB, 0.dB],
//   preambleDuration: 5.ms,
//   txDelay:          0,
//   clockOffset:      0,
//   headerLength:     0
// ]





channel = [
  model:              org.arl.unet.sim.channels.ProtocolChannelModel,
  soundSpeed: 1500.mps,
  communicationRange: 1500.m,
  detectionRange: 2000.m,
  interferenceRange: 1500.m,
  pDetection:         1,
  pDecoding:          1
]

modem.dataRate = [2400, 2400].bps
modem.frameLength = [4, 300].bytes
modem.preambleDuration = 0
modem.txDelay = 0
modem.clockOffset = 0.s
modem.headerLength = 0.s

// Simulation settings

def node_count = 9
def load_range = [0.1, 0.3, 0.1] 
def T = 100.minutes                       // simulation horizon
trace.warmup =  10.minutes             // collect statistics after a while


File out = new File("logs/results.txt")
out.text = ''


node_location = []

for(int x = -1; x<= 1; x++){
    for(int y = -1; y<= 1;y++){
        node_location.add([x*1000, y*1000, -30.m])
    }
}

def address_list = []
for(int i = 0;i<node_count;i++){
    address_list.add((i+11))
}

def web_list = []
def web_base = RandomUtils.nextInt(6000,9000)
for(int i = 0;i<node_count;i++){
    web_list.add((i+web_base))
}

def api_list = []
for(int i = 0;i<node_count;i++){
    api_list.add((i+1101))
}


def sum = 0
def propagation_delay = new Integer[node_count][node_count]
for(int i = 0; i < node_count; i++){
  for(int j = 0; j < node_count; j++){
    // if (n1 < n2) {
    //   n++
    //   sum += distance(nodeLocation[n1], nodeLocation[n2])
    // }
    propagation_delay[i][j] = (int)(distance(node_location[i],node_location[j]) * 1000 / channel.soundSpeed + 0.5)
  }
}



for (def load = load_range[0]; load <= load_range[1]; load += load_range[2]) {
    def node_list = []
    def mac_list = []
    simulate {
        
        for(def n = 0; n < node_count; n++){
            macAgent = new MyMac()
            mac_list.add(macAgent)
            node_list.add(node("node${n+1}", web: web_list[n], api: api_list[n], address: address_list[n], location: node_location[n], stack:{ container ->   
            
            container.add 'arp',            new org.arl.unet.addr.AddressResolution()
            container.add 'ranging',        new org.arl.unet.localization.Ranging()
            container.add 'uwlink',         new org.arl.unet.link.ReliableLink()
            container.add 'transport',      new org.arl.unet.transport.SWTransport()
            container.add 'router',         new org.arl.unet.net.Router()
            container.add 'rdp',            new org.arl.unet.net.RouteDiscoveryProtocol()
            // container.add 'statemanager',   new org.arl.unet.state.StateManager()
            // container.add 'remote',         new org.arl.unet.remote.RemoteControl(cwd: new File(home, 'scripts'), enable: false)
            // container.add 'bbmon',          new org.arl.unet.bb.BasebandSignalMonitor(new File(home, 'logs/signals-0.txt').path, 64)
            
            container.add 'mac', mac_list[n]
            }))


            // macAgent = mac_list[n
            assert macAgent != null
            // println macAgent.getClass()
            // macAgent.propagationDelay = propagation_delay
            for(int i = 0;i<address_list.size();i++){
                for(int j = 0;j<address_list.size();j++){
                    if(propagation_delay[i][j] != propagation_delay[j][i]){
                        log.warning 'ERROR IN PROPAGATION_DELAY_PARAMETER'
                    }else if(i == j && propagation_delay[i][i] != 0.0){
                        log.warning 'ERROR IN PROPAGATION_DELAY_PARAMETER'
                    }
                    else{
                        macAgent.propagationDelay.add( propagation_delay[i][j])
                    }
                    println propagation_delay[i].getClass()
                    println macAgent.propagationDelay.getClass()
                    println macAgent.propagationDelay.getClass()
                    // macAgent.propagationDelay.add(propagation_delay[i])
                }
                
                
            } 
            
            // node_list.add(node("${n+1}", web: web_list[n], api: api_list[n], address: address_list[n], location: node_location[n], stack:"$home/scripts/attach.groovy"))
            macAgent.dataMsgDuration = (int)(8000*modem.frameLength[1]/modem.dataRate[1] + 0.5)
            macAgent.controlMsgDuration = (int)(8000*modem.frameLength[0]/modem.dataRate[0] + 0.5)
            // macAgent.setNodeList(address_list)
            log.fine "HIT"
            macAgent.nodeList = address_list
            log.fine n.toString()
            
        }
    }
    log.fine "THERE"
}


// def propagation_delay = new Integer[nodes.size()][nodes.size()]

// for(int s = 0; s< nodes.size(); s++){
//     for(int t = 0; t< nodes.size();t++){
//         propagation_delay[s][t] = (int)(distance(node_location[s],node_location[t]) * 1000 / channel.soundSpeed + 0.5)
//     }
// }

// ///////////////////////////////////////////////////////////////////////////////
// // simulation details

// File out = new File("logs/results.txt")
// out.text = ''

// def node_list = []
// nodes.each { n ->
//     node_list << node("${n+1}", web: web_list[n], api: api_list[n], address: address_list[n], location: node_location[n], stack: "$home/scripts/attach.groovy")  
// }

// // simulate at various arrival rates
// for (def load = load_range[0]; load <= load_range[1]; load += load_range[2]) {
//   simulate {

//     // setup 4 nodes identically
//     nodes.each { n ->
    
//         // float loadPerNode = load/nodes.size()   
//         // def macAgent = new MyMac()
//         // node_list << node("${n+1}", web: web_list[n], api: api_list[n], address: address_list[n], location: node_location[n], stack: "$home/scripts/attach.groovy")  
//         // container.kill 'mac'
//         // container.add 'mac', macAgent
//         // container.add 'load', new LoadGenerator(nodes-n, loadPerNode)
//         // macAgent.dataMsgDuration = (int)(8000*modem.frameLength[1]/modem.dataRate[1] + 0.5)
//         // macAgent.controlMsgDuration = (int)(8000*modem.frameLength[0]/modem.dataRate[0] + 0.5)
//         // macAgent.nodeList  = address_list
//         // for(int i = 0;i<address_list.size();i++)
//         // {
//         //     for(int j = 0;j<address_list.size();j++)
//         //     {
//         //     if(propagation_delay[i][j] != propagation_delay[j][i])
//         //     {
//         //         log.warning 'ERROR IN PROPAGATION_DELAY_PARAMETER'
//         //     }  
//         //     else
//         //     {
//         //         if(i == j && propagation_delay[i][i] != 0.0)
//         //         {
//         //         log.warning 'ERROR IN PROPAGATION_DELAY_PARAMETER'
//         //         }
//         //         else
//         //         {
//         //         macAgent.propagationDelay.add(propagation_delay[i][j])                                     
//         //         }            
//         //     }  
//         //     }
//         // } 


//     } // each



//   }  // simulate

//   // display statistics
//   float loss = trace.txCount ? 100*trace.dropCount/trace.txCount : 0
//   println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
//     [trace.txCount, trace.rxCount, loss, load, trace.throughput])

//   // save to file
//   out << "${trace.offeredLoad},${trace.throughput}\n"

// }


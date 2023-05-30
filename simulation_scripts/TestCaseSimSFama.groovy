//! Simulation: Slotted FAMA
///////////////////////////////////////////////////////////////////////////////
/// 
/// To run simulation:
///   bin/unet Slotted FAMA/TestCaseSim.groovy
///
/// Output trace file: logs/trace.nam
///
///////////////////////////////////-////////////////////////////////////////////

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import static org.arl.unet.Services.*
import static org.arl.unet.phy.Physical.*
import MAC.*
import SetupAgents.*

println '''
Slotted-FAMA simulation
===================
'''

///////////////////////////////////////////////////////////////////////////////
// modem and channel model to use

modem.dataRate = [2400, 2400].bps
modem.frameLength = [4, 300].bytes
modem.preambleDuration = 0
modem.txDelay = 0
modem.clockOffset = 0.s
modem.headerLength = 0.s


range = 2.km

channel.model = ProtocolChannelModel
channel.soundSpeed = 1500.mps
channel.communicationRange = range
channel.interferenceRange = range
channel.detectionRange = range



///////////////////////////////////////////////////////////////////////////////
// simulation settings

def node_count = 9
def nodes = []
for(i = 1; i <= node_count; i++){
    nodes.add(i) // list of nodes
}                   
def loadRange = [0.1, 1.5, 0.1] 
def T = 60.minutes                       // simulation horizon
trace.warmup =  10.minutes             // collect statistics after a while

///////////////////////////////////////////////////////////////////////////////
// simulation details

// generate random network geometry
locations = [
    [-1.km,  1.km, -10.m],
    [ 0.km,  1.km, -10.m],
    [ 1.km,  1.km, -10.m],
    [-1.km,  0.km, -10.m],
    [ 0.km,  0.km, -10.m],
    [ 1.km,  0.km, -10.m],
    [-1.km, -1.km, -10.m],
    [ 0.km, -1.km, -10.m],
    [ 1.km, -1.km, -10.m],
]
//Deploying nodes randomly within a square with uniform distribution 


def nodeLocation = [:]
nodes.each { myAddr ->
//   nodeLocation[myAddr] = [rnd(0.km, 3.0.km), rnd(0.km, 3.0.km), -10.m]
    nodeLocation[myAddr] = locations[myAddr-1]
}



def apiList = [:]
nodes.each { myAddr ->
  apiList[myAddr] = 1105+myAddr
}

def webList = [:]
nodes.each { myAddr ->
  webList[myAddr] = 8081+myAddr
}



def addressList = new ArrayList<Integer>()
for(int i = 0;i<nodes.size();i++)
{
  addressList.add((i+1))
}

// compute average distance between nodes for display
def sum = 0
def n = 0
def maxPropagationDelay = 0
def propagationDelay = new Integer[nodes.size()][nodes.size()]
nodes.each { n1 ->
  nodes.each { n2 ->
    if (n1 < n2) {
      n++
      sum += distance(nodeLocation[n1], nodeLocation[n2])
    }
    propagationDelay[n1-1][n2-1] = (int)(distance(nodeLocation[n1],nodeLocation[n2]) * 1000 / channel.soundSpeed + 0.5)
  }
}
def avgRange = sum/n
println """Average internode distance: ${Math.round(avgRange)} m, delay: ${Math.round(1000*avgRange/channel.soundSpeed)} ms

TX Count\tRX Count\tLoss %\t\tOffered Load\tThroughput
--------\t--------\t------\t\t------------\t----------"""   

File out = new File("logs/resultsSFama2.csv")
out.text = ''

// simulate at various arrival rates
for (def load = loadRange[0]; load <= loadRange[1]; load += loadRange[2]) {
    simulate T, {
        def node_list = []
        // setup nodes 
        nodes.each { myAddr ->
        
            float loadPerNode = load/nodes.size()     
            // divide network load across nodes evenly
            def macAgent = new MAC.MySlottedFama()
            node_list << node("${myAddr}", address: myAddr, location: nodeLocation[myAddr] , web: webList[myAddr], api:apiList[myAddr],  stack : { container -> 
            container.add 'arp',            new org.arl.unet.addr.AddressResolution()
            container.add 'ranging',        new org.arl.unet.localization.Ranging()
            container.add 'uwlink',         new org.arl.unet.link.ReliableLink()
            container.add 'transport',      new org.arl.unet.transport.SWTransport()
            container.add 'router',         new org.arl.unet.net.Router()
            container.add 'rdp',            new org.arl.unet.net.RouteDiscoveryProtocol()
            container.add 'mac', macAgent 
            }) 

            for(int i = 0; i<nodes.size(); i++)
            {
                for(int j = 0; j<nodes.size(); j++)
                {
                if(propagationDelay[i][j] > maxPropagationDelay)
                {
                    maxPropagationDelay = propagationDelay[i][j]
                }
                } 
            }        

            macAgent.timerCtsTimeoutOpMode = 2
            macAgent.maxPropagationDelay = maxPropagationDelay
            macAgent.dataMsgDuration = (int)(8000*modem.frameLength[1]/modem.dataRate[1] + 0.5)
            macAgent.controlMsgDuration = (int)(8000*modem.frameLength[0]/modem.dataRate[0] + 0.5)      
            destNodes = nodes.minus([myAddr])
            container.add 'load', new SetupAgents.OldLoadGenerator(destNodes, loadPerNode)    

    } // each       

  }  // simulate

  // display statistics
  float loss = trace.txCount ? 100*trace.dropCount/trace.txCount : 0
  println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
    [trace.txCount, trace.rxCount, loss, load, trace.throughput])

  // save to file
  out << "${trace.txCount}, ${trace.rxCount}, ${loss}, ${load},${trace.offeredLoad}, ${trace.throughput}\n"


} 


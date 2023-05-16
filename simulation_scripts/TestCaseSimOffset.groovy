import org.arl.unet.mac.CSMA
import groovy.lang.MissingMethodException
import org.apache.commons.lang3.time.DateUtils
import org.apache.commons.lang3.RandomUtils
//! Simulation: ALOHA-AN
///////////////////////////////////////////////////////////////////////////////
/// 
/// To run simulation:
///   bin/unet ALOHA-AN/TestCaseSim.groovy
///
/// Output trace file: logs/trace.nam
/// Reference:
/// N. Chirdchoo, W.S. Soh, K.C. Chua (2007), Aloha-based MAC Protocols with
/// Collision Avoidance for Underwater Acoustic Networks, in Proceedings of
/// IEEE INFOCOM 2007.
/// Modified and adapted by A.C.Fleming (QinetiQ Australia 05/2023)
///////////////////////////////////-////////////////////////////////////////////

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import static org.arl.unet.Services.*
import static org.arl.unet.phy.Physical.*
import org.arl.fjage.Agent.*
// import java.util.Date;
import java.text.SimpleDateFormat
import org.arl.unet.sim.Tracer
import org.arl.unet.sim.NamTracer



println '''
Simulation
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

channel.model = ProtocolChannelModel
channel.soundSpeed = 1500.mps
channel.communicationRange = 5.km
channel.interferenceRange = 5.km
channel.detectionRange = 5.km

// platform = org.arl.fjage.RealTimePlatform

///////////////////////////////////////////////////////////////////////////////
// simulation settings
def node_count = 9

def load_range = [0.1, 1.5, 0.1] 
def T = 100.minutes                       // simulation horizon
// trace.warmup =  0.minutes             // collect statistics after a while

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


transmitters = [
 false,
 false,
 false,
 false,
 false,
 false,
 false,
 false,
 false
]


///////////////////////////////////////////////////////////////////////////////
// simulation details

def node_locations = []
def api_list = []
def web_list = []
def address_list = []
def tx_flag = []
def api_base = 1101
def web_base = 8081
def address_base = 1
for(int i = 0; i < node_count; i++){
    def theta = RandomUtils.nextFloat(0, 2*3.14159)
    def radius = RandomUtils.nextFloat(0,500)
    pos = [locations[i][0]+radius*Math.cos(theta), locations[i][1]+radius*Math.sin(theta)]
    node_locations.add(pos)
    println pos
    api_list.add(api_base+i)
    web_list.add(web_base+i)
    address_list.add(address_base+i)
    tx_flag.add(transmitters[i])
}


def mac_name = "ALOHA"
def scenario_name = "counting"
def date = new Date()
def sdf = new SimpleDateFormat("HH-mm-ss")
def time =  sdf.format(date)
def file_name = "results/" + mac_name  + "_"+ scenario_name
// file_name +=  "_"+ time

 
println "  {ld}  ,  {dC}  ,  {eC}  ,  {sL}  ,  {mD}  ,  {oL}  ,  {rx}  ,  {tp}  ,  {tx}  ,  {ls}"



File out = new File(file_name)
out.text = ''
out << "{load}, {dropCount}, {enqueCount}, {simLoad}, {meanDelay}, {offeredLoard}, {rxCount}, {throughput}, {txCount}, {loss}\n"

// simulate at various arrival rates
for (def load = load_range[0]; load <= load_range[1]; load += load_range[2]) {
    
    simulate {

        def node_list = []

        // setup 4 nodes identically
        for(int n = 0; n < node_count; n++ ){
            // divide network load across nodes evenly
            // print "Nodes in test sim"
            // print nodes.size()

            float loadPerNode = load/node_count    
            
            def macAgent = new AlohaAN()
            switch(mac_name) {
                case "ALOHA":
                    macAgent = new AlohaAN()
                break
                case "SFAMA":
                    macAgent = new SlottedFama()
                break
                case "CSMA":
                    macAgent = new MyCSMA()
                break
                default:
                    macAgent = new AlohaAN()
                break
            }

            
            
            node_list << node("Node${n}", address: address_list[n], location: node_locations[n] , web: web_list[n], api:api_list[n],  stack : { container -> 
            container.add 'arp',            new org.arl.unet.addr.AddressResolution()
            container.add 'ranging',        new org.arl.unet.localization.Ranging()
            container.add 'uwlink',         new org.arl.unet.link.ReliableLink()
            container.add 'transport',      new org.arl.unet.transport.SWTransport()
            container.add 'router',         new org.arl.unet.net.Router()
            container.add 'rdp',            new org.arl.unet.net.RouteDiscoveryProtocol()
            container.add 'mac',            macAgent 
            })    
            try{
                macAgent.initParams(address_list,node_locations,channel, modem)
            } catch (MissingMethodException e1){
                println e1.toString()
                
            }
            
            destNodes = address_list.minus(address_list[n])
            if(tx_flag[n] == true){
                container.add 'load', new TransferGenerator(destNodes, loadPerNode) 
            }
            
        } // each



    }  // simulate
    // assert trace.getClass() == NamTracer
    // display statistics
    // def dropCount = trace.getDropCount().round(4)
    // def enqueCount = trace.getEnqueueCount().round(4)
    // def simLoad = trace.getLoad().round(4)
    // def meanDelay = trace.getMeanDelay().round(4)
    // def offeredLoard = trace.getOfferedLoad().round(4)
    // def rxCount = trace.getRxCount().round(4)
    // def throughput = trace.getThroughput().round(4)
    // def txCount = trace.getTxCount().round(4)
    // def loss = trace.txCount ? (trace.dropCount/trace.txCount).round(4) : 0
    // loadVal = load.round(4)
    def dropCount       = String.format("%07.3f",trace.getDropCount().round(4))
    def enqueCount      = String.format("%07.3f", trace.getEnqueueCount().round(4))
    def simLoad         = String.format("%07.3f", trace.getLoad().round(4))
    def meanDelay       = String.format("%07.3f", trace.getMeanDelay().round(4))
    def offeredLoard    = String.format("%07.3f", trace.getOfferedLoad().round(4))
    def rxCount         = String.format("%07.3f", trace.getRxCount().round(4))
    def throughput      = String.format("%07.3f",trace.getThroughput().round(4))
    def txCount         = String.format("%07.3f", trace.getTxCount().round(4))
    def loss            = trace.txCount ? (trace.dropCount/trace.txCount).round(4) : 0
    loss                = String.format("%07.3f",loss.round(4))
    loadVal             = String.format("%07.3f", load.round(4))
    
    // println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
    //     [trace.txCount, trace.rxCount, loss, load, trace.throughput, trace.offeredLoad, tra])
    println "${loadVal}, ${dropCount}, ${enqueCount}, ${simLoad}, ${meanDelay}, ${offeredLoard}, ${rxCount}, ${throughput}, ${txCount}, ${loss}"

    // save to file
    out << "${loadVal}, ${dropCount}, ${enqueCount}, ${simLoad}, ${meanDelay}, ${offeredLoard}, ${rxCount}, ${throughput}, ${txCount}, ${loss}\n"

}

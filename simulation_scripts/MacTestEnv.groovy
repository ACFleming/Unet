
// MAC Testing Environment
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
import org.arl.unet.net.*
import org.arl.unet.mac.*
import org.arl.unet.sim.channels.*
import static org.arl.unet.Services.*
import static org.arl.unet.phy.Physical.*
import org.arl.fjage.Agent.*
import java.text.SimpleDateFormat
import groovy.lang.MissingMethodException
import org.apache.commons.lang3.time.DateUtils
import Scenarios.*
import MAC.*
import SetupAgents.*



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
channel.communicationRange = 1.5.km
channel.interferenceRange = 1.5.km
channel.detectionRange = 1.5.km

// platform = org.arl.fjage.RealTimePlatform
 



BaseScenario b = new BaseScenario()
def mac_name = "CSMA"
def scenario_name = b.getFileString()
def file_name = "results/" + mac_name  + "_"+ scenario_name

 
println "  {ld}  ,   {dC}  ,   {eC}  ,   {sL}  ,   {mD}  ,   {oL}  ,   {rx}  ,   {tp}  ,   {tx}  ,   {ls}"
File out = new File(file_name)
out.text = ''
out << "{load}, {dropCount}, {enqueCount}, {simLoad}, {meanDelay}, {offeredLoard}, {rxCount}, {throughput}, {txCount}, {loss}\n"






// simulate at various arrival rates
for (def load = b.load_range[0]; load <= b.load_range[1]; load += b.load_range[2]) {
    // print address_list
    simulate b.T, {

        def node_list = []

        // setup 4 nodes identically
        for(int n = 0; n < b.node_count; n++ ){


            float loadPerNode = load/b.node_count    
            
            def macAgent = b.macs[mac_name].newInstance()
            // print "${macAgent.getClass()}"
            macAgent.initParams(b.address_list,b.node_locations,b.channel, b.modem)
            
            
            node_list << node("Node${n+1}", address: b.address_list[n], location: b.node_locations[n] , web: b.web_list[n], api:b.api_list[n],  stack : { container -> 
            container.add 'arp',            new org.arl.unet.addr.AddressResolution()
            container.add 'ranging',        new org.arl.unet.localization.Ranging()
            container.add 'uwlink',         new org.arl.unet.link.ReliableLink()
            container.add 'transport',      new org.arl.unet.transport.SWTransport()
            container.add 'router',         new org.arl.unet.net.Router()
            container.add 'rdp',            new org.arl.unet.net.RouteDiscoveryProtocol()
            container.add 'mac',            macAgent 
            
            }) 

            def l = b.getGenerator(n, loadPerNode)
            container.add 'load', l
            RouteAdder r = b.getAddder(n)
            container.add 'addRoutes', r
                       
            
        } // each



    }  // simulate
    def dropCount       = String.format("%08.3f",trace.getDropCount().round(4))
    def enqueCount      = String.format("%08.3f", trace.getEnqueueCount().round(4))
    def simLoad         = String.format("%08.3f", trace.getLoad().round(4))
    def meanDelay       = String.format("%08.3f", trace.getMeanDelay().round(4))
    def offeredLoard    = String.format("%08.3f", trace.getOfferedLoad().round(4))
    def rxCount         = String.format("%08.3f", trace.getRxCount().round(4))
    def throughput      = String.format("%08.3f",trace.getThroughput().round(4))
    def txCount         = String.format("%08.3f", trace.getTxCount().round(4))
    def loss            = trace.txCount ? (trace.dropCount/trace.txCount).round(4) : 0
    loss                = String.format("%08.3f",loss.round(4))
    loadVal             = String.format("%08.3f", load.round(4))
    
    // println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
    //     [trace.txCount, trace.rxCount, loss, load, trace.throughput, trace.offeredLoad, tra])
    println "${loadVal}, ${dropCount}, ${enqueCount}, ${simLoad}, ${meanDelay}, ${offeredLoard}, ${rxCount}, ${throughput}, ${txCount}, ${loss}"

    // save to file
    out << "${loadVal}, ${dropCount}, ${enqueCount}, ${simLoad}, ${meanDelay}, ${offeredLoard}, ${rxCount}, ${throughput}, ${txCount}, ${loss}\n"

}


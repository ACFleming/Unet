
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
import Scenarios.LowQuantityScenario
import Scenarios.BaseScenario
import Scenarios.OffsetScenario
import org.apache.commons.math3.ml.clustering.Cluster
import Scenarios.ClusterScenario
import Scenarios.CollinearScenario
import Scenarios.HighQuantityScenario
import Scenarios.EquidistantScenario
// import Scenarios.OffsetScenario
// import Scenarios.HighQuantityScenario
// import Scenarios.BaseScenario
import Scenarios.*
import MAC.*
import SetupAgents.*



println '''
Simulation
===================
'''

///////////////////////////////////////////////////////////////////////////////
// modem and channel model to use



// platform = org.arl.fjage.RealTimePlatform
 

scenarios = []

def b = new BaseScenario()
def o = new OffsetScenario()
def h = new HighQuantityScenario()
def l = new LowQuantityScenario()
def c = new ClusterScenario()
def cl = new CollinearScenario()
def e = new EquidistantScenario()


def scenario = l

// def mac_name = "ALOHA"

// def mac_types = b.getMacs()



// simulate at various arrival rates
for (m in scenario.getMacs()){

    def mac_name = m.key
    if(mac_name != "SFAMA"){
        continue
    }

    def scenario_name = scenario.getFileString()
    def file_name = "results/" + mac_name  + "_"+ scenario_name +".csv"
    print "Testing ${scenario.getFileString()} with ${mac_name}\n"

    println "  {ld}  ,   {dC}  ,   {eC}  ,   {sL}  ,   {mD}  ,   {oL}  ,   {rx}  ,   {tp}  ,   {tx}  ,   {ls}"
    File out = new File(file_name)
    out.text = ''
    out << "{load}, {dropCount}, {enqueCount}, {simLoad}, {meanDelay}, {offeredLoard}, {rxCount}, {throughput}, {txCount}, {loss}\n"



    for (def load = scenario.load_range[0]; load <= scenario.load_range[1]; load += scenario.load_range[2]) {
        simulate  scenario.getT(), {

            def node_list = []

            // setup 4 nodes identically
            for(int n = 0; n < scenario.getNodeCount(); n++ ){
                

                float loadPerNode = load/scenario.getNodeCount()
        
                
                def macAgent = scenario.getMacType(mac_name).newInstance()
                
                macAgent.initParams(scenario.getAddressList(),scenario.getNodeLocations(),scenario.getChannel(), scenario.getModem())
                // print "INIT"
                
                
                node_list << node("Node${n+1}", address: scenario.getAddressList()[n], location: scenario.getNodeLocations()[n] , web: scenario.getWebList()[n], api:scenario.getApiList()[n],  stack : { container -> 
                container.add 'arp',            new org.arl.unet.addr.AddressResolution()
                container.add 'ranging',        new org.arl.unet.localization.Ranging()
                container.add 'uwlink',         new org.arl.unet.link.ReliableLink()
                container.add 'transport',      new org.arl.unet.transport.SWTransport()
                container.add 'router',         new org.arl.unet.net.Router()
                container.add 'rdp',            new org.arl.unet.net.RouteDiscoveryProtocol()
                container.add 'mac',            macAgent 
                
                }) 
                
                // print "Loads and routes\n"
                def ld = scenario.getGenerator(n, loadPerNode)
                container.add 'load', ld
                RouteAdder r = scenario.getAdder(n)
                container.add 'addRoutes', r
                // print "${n} Node ${scenario.getAddressList()[n]} finished\n"           
                
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

}










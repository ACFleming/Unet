//! Simulation

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import SetupAgents.TickAgent
// import org.codehaus.groovy.runtime.ProcessGroovyMethods$ProcessRunner


///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel and modem settings


modem.model =  org.arl.unet.sim.HalfDuplexModem

channel = [
    model:                  BasicAcousticChannel,
    carrierFrequency:       25000.Hz, 
    bandwidth:              4096.Hz, 
    spreading:              2,
    temperature:            25.C,
    salinity:               35.ppt,
    noiseLevel:             60.dB,
    waterDepth:             20.m,
    ricianK:                10, 
    fastFading:             true, 
    pfa:                    1e-6, 
    processingGain:         0.dB, 
]
///////////////////////////////////////////////////////////////////////////////
// simulation details

int node_count = 4
try{
    if(this.args.size() != 0){
        node_count = this.args[0].toInteger()
    }
} catch (Exception e){
    println "Usage: bin/unet script [number of nodes]"
    exit()
}
// long pid = ProcessHandle.current().pid();
// println pid

println node_count
origin=  [0,0]
simulate {
    def nodes = []
    for(int n = 1; n <= node_count; n++){
        def shell_on = (n==1) ? true: false
        nodes << node("Node ${n}",  location: [ n*1.km,  0.km, -20.m], address: 10+n, web: 8080+n, api: 1100+n, mobility: true, shell: shell_on , stack : { container -> 
                container.add 'arp',            new org.arl.unet.addr.AddressResolution()
                container.add 'ranging',        new org.arl.unet.localization.Ranging()
                container.add 'uwlink',         new org.arl.unet.link.ReliableLink()
                container.add 'transport',      new org.arl.unet.transport.SWTransport()
                container.add 'router',         new org.arl.unet.net.Router()
                container.add 'rdp',            new org.arl.unet.net.RouteDiscoveryProtocol()            
                })
        println "Created Node ${n} with address ${10+n}, web port ${8080+n} and api port ${1100 + n}"
        
    }
    println "Init Complete"
}
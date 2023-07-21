//! Simulation

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import SetupAgents.TickAgent

///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel and modem settings


modem.model =  org.arl.unet.sim.HalfDuplexModem
// modem.janus = [true, true, true]

println this.args

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

simulate {
    
    def n1 = node('Command',  location: [ 0.km,  0.km, -20.m], address: 11, web: 8081, api: 1101, mobility: true,  stack : { container -> 
                container.add 'arp',            new org.arl.unet.addr.AddressResolution()
                container.add 'ranging',        new org.arl.unet.localization.Ranging()
                container.add 'uwlink',         new org.arl.unet.link.ReliableLink()
                container.add 'transport',      new org.arl.unet.transport.SWTransport()
                container.add 'router',         new org.arl.unet.net.Router()
                container.add 'rdp',            new org.arl.unet.net.RouteDiscoveryProtocol()
                container.add 'tick',           new TickAgent()
                
                }) 
    def n2 = node('Follower', location: [ 0.km,  4.km, -20.m], address: 12, web: 8082, api: 1102, mobility: true,  stack : { container -> 
                container.add 'arp',            new org.arl.unet.addr.AddressResolution()
                container.add 'ranging',        new org.arl.unet.localization.Ranging()
                container.add 'uwlink',         new org.arl.unet.link.ReliableLink()
                container.add 'transport',      new org.arl.unet.transport.SWTransport()
                container.add 'router',         new org.arl.unet.net.Router()
                container.add 'rdp',            new org.arl.unet.net.RouteDiscoveryProtocol()
                
                }) 
  
}
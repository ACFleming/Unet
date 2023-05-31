//! Simulation

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*

///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel and modem settings


modem.model =  org.arl.unet.sim.HalfDuplexModem
// modem.janus = [true, true, true]



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
    
    def n1 = node 'N', location: [ 0.km,  1.km, -20.m], address: 11, web: 8081, api: 1101, stack: "$home/etc/setup"
    def n2 = node 'E', location: [ 1.km,  2.km, -20.m], address: 12, web: 8082, api: 1102, stack: "$home/etc/setup"
    def n3 = node 'S', location: [ 0.km, -1.km, -20.m], address: 13, web: 8083, api: 1103, stack: "$home/etc/setup" 
    def n4 = node 'W', location: [-1.km,  0.km, -20.m], address: 14, web: 8084, api: 1104, stack: "$home/etc/setup" 
  
}
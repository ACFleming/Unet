//! Simulation

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*

///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel and modem settings

channel = [
    model:                BasicAcousticChannel,
    carrierFrequency:     11520.Hz, // (the default for JANUS)
    bandwidth:            4160.Hz, // (the default for JANUS)
    spreading:            1.5,
    temperature:          25.C,
    salinity:             35.ppt,
    noiseLevel:           60.dB,
    waterDepth:           20.m,
    ricianK : 10, // K
    fastFading : true, // fast/slow fading
    pfa:  1e-6, // pfa
    processingGain: 0.dB, // G
]


///////////////////////////////////////////////////////////////////////////////
// simulation details

simulate {
  def s = node 'Here' , location: [0.m, 0.m,  -50.m] ,web: 8081, api: 1101, stack: "$home/etc/setup.groovy"
  def l = node 'There', location: [0.m, 1000.m,  -50.m],web: 8082, api: 1102, stack: "$home/scripts/attach.groovy", mobility: true
}
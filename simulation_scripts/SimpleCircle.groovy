//! Simulation

import org.arl.fjage.RealTimePlatform
import Scenarios.OffsetScenario
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import Scenarios.*


///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel and modem settings


// modem.model =  org.arl.unet.sim.HalfDuplexModem



channel = [
    model:                BasicAcousticChannel,
    carrierFrequency:     11520.Hz, // (the default for JANUS)
    bandwidth:            4160.Hz, // (the default for JANUS)
    spreading:            1.5,
    temperature:          25.C,
    salinity:             35.ppt,
    noiseLevel:           60.dB,
    waterDepth:           20.m,
    ricianK:              10, // K
    fastFading:           true, // fast/slow fading
    pfa:                  1e-6, // pfa
    processingGain:       0.dB, // G
]

// trace.warmup =  10.minutes 
///////////////////////////////////////////////////////////////////////////////
// simulation details
println trace

BaseScenario b = new OffsetScenario()

simulate {
  def n1 = node 'I', address: 21, location: [-30.m, 0.m,  -5.m], web: 8081, api: 1101, shell: true
  def n2 = node 'J', address: 22, location: [90.m, 0.m, -10.m], web: 8082, api: 1102
  def n3 = node 'K', address: 23, location: [-15.m, 0.m, -15.m], web: 8083, api: 1103, mobility: true
  n3.motionModel = [speed: 10.mps, turnRate: 5.dps]
}
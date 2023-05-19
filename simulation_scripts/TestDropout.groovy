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
    def n = node 'B', location: [ 1.km, 0.km, -15.m], web: 8082, api: 1102, stack: "$home/etc/setup", mobility: true
    counter = 0
    n.motionModel = { ts -> 
                    def setpoint = [speed: 100.mps, duration: 18.seconds]
                    // def cycle_time = ts.remainder(108)
                    println ts
                    println cycle_time
                    // if(cycle_time == 0){
                    //     setpoint['heading'] = 0.deg
                    // }else if (cycle_time <= 18.seconds){
                    //     setpoint["turnRate"] = 10.dps
                    // }else if (cycle_time <= 36.seconds){
                    //     setpoint["turnRate"] = -10.dps
                    // }else if (cycle_time <= 72.seconds){
                    //     setpoint["turnRate"] = 10.dps
                    // }else if (cycle_time <= 90.seconds){
                    //     setpoint["turnRate"] = -10.dps
                    // }else{
                    //     setpoint["turnRate"] = 10.dps
                    // }
                    return setpoint
                }
}
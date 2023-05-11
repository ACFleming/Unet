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
  carrierFrequency:     25.kHz,
  bandwidth:            4096.Hz,
  spreading:            1.5,
  temperature:          25.C,
  salinity:             35.ppt,
  noiseLevel:           60.dB,
  waterDepth:           20.m
]

modem.dataRate =        [800.bps, 2400.bps]
modem.frameLength =     [16.bytes, 64.bytes]
modem.powerLevel =      [0.dB, -10.dB]

///////////////////////////////////////////////////////////////////////////////
// simulation details

simulate {
  node '1', address: 21, web: 8081, api: 1101, location: [0.km, 0.km,  -5.m], stack: "$home/scripts/attach.groovy", shell:true
}
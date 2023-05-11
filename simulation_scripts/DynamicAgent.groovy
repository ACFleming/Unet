//! Simulation
import org.arl.fjage.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*


///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel and modem settings

channel = [
  model:                BasicAcousticChannel,
  carrierFrequency:     50.kHz,
  bandwidth:            8192.Hz,
  spreading:            1.5,
  temperature:          25.C,
  salinity:             35.ppt,
  noiseLevel:           60.dB,
  waterDepth:           1.km
]


///////////////////////////////////////////////////////////////////////////////
// simulation details

// loc = new double[3];
// loc[0] = 1;
// loc[1] = 1;
// loc[2] = -5;


simulate {
  def s = node 'Here' , location: [0.m, 0.m,  -50.m] ,web: 8081, api: 1101, stack: "$home/etc/setup.groovy",mobility: true
  def l = node 'There', location: [0.m, 1000.m,  -50.m],web: 8082, api: 1102, stack: "$home/etc/setup.groovy", mobility: true

}
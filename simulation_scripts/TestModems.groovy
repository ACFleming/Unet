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
    model:              BasicAcousticChannel,
    carrierFrequency:   11520.Hz, // (the default for JANUS)
    bandwidth:          4160.Hz, // (the default for JANUS)
    spreading:          1.5,
    temperature:        25.C,
    salinity:           35.ppt,
    noiseLevel:         60.dB,
    waterDepth:         20.m,
    ricianK:            10, 
    fastFading:         true, 
    pfa:                1e-6, 
    processingGain:     0.dB, 
]


///////////////////////////////////////////////////////////////////////////////
// simulation details



simulate {
    def n0 = node 'N0', web: 8081, api: 1101, location: [  0.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    def n1 = node 'N1', web: 8082, api: 1102, location: [10.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    def n2 = node 'N2', web: 8083, api: 1103, location: [100.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    def n3 = node 'N3', web: 8084, api: 1104, location: [1000.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    // def n4 = node 'N4', web: 8085, api: 1105, location: [400.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    // def n5 = node 'N5', web: 8086, api: 1106, location: [500.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    // def n6 = node 'N6', web: 8087, api: 1107, location: [600.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    // def n7 = node 'N7', web: 8088, api: 1108, location: [700.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    // def n8 = node 'N8', web: 8089, api: 1109, location: [800.m, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true
    // def n9 = node 'N9', web: 8090, api: 1110, location: [1.km, 0.m,  -5.m], stack: "$home/etc/setup.groovy", mobility:true


}
package Scenarios

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
import MAC.SlottedFama
import MAC.MyCSMA
import MAC.AlohaAN
// import SetupAgents.RouteAdder
import SetupAgents.*
// import SetupAgents.RouteAdder




class BaseScenario{

    public def T = 100.minutes  
    public def load_range = [0.1, 1.0, 0.2] 

    public def modem = [
            model:              org.arl.unet.sim.HalfDuplexModem,
            dataRate:           [2400.bps, 2400.bps],
            frameLength:        [16.bytes, 300.bytes],
            powerLevel:         [0.dB, -10.dB],
            preambleDuration:   0,
            txDelay:            0,
            clockOffset:        0.s,
            headerLength:       0.s
            ]
    public def channel = [
                    model:              org.arl.unet.sim.channels.ProtocolChannelModel,
                    soundSpeed:         1500.mps,
                    communicationRange: 1500.m,
                    interferenceRange:  1500.m,
                    detectionRange:     2500.m,
                    pDetection:         1,
                    pDecoding:          1
                ]
    public def api_base = 1101
    public def web_base = 8081
    public def address_base = 10
    public def api_list = []
    public def web_list = []
    public def address_list = []



    public def macs = [:]


    public def node_count
    public def node_locations
    public def transmitters = []
    public def routing_steps = []
    public def routing_dist = []
    public def dest_nodes = []
    

    BaseScenario(){

        this.node_count = 9
        this.node_locations = [
                [-0.5.km,  0.5.km, -10.m],
                [ 0.km,  0.5.km, -10.m],
                [ 0.5.km,  0.5.km, -10.m],
                [-0.5.km,  0.km, -10.m],
                [ 0.km,  0.km, -10.m],
                [ 0.5.km,  0.km, -10.m],
                [-0.5.km, -0.5.km, -10.m],
                [ 0.km, -0.5.km, -10.m],
                [ 0.5.km, -0.5.km, -10.m],
            ]
        this.transmitters = [
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true
                ]


        for(def i = 0; i < this.node_count; i++){
            
            api_list.add(api_base+i)
            web_list.add(web_base+i)
            address_list.add(address_base+i)

        }
        for(def i = 0; i < this.node_count; i++){
            def inner = []
            for(def j = 0; j < this.node_count; j++){
                if(i!=j){
                    inner.add(address_list[j])
                }
            }
            this.dest_nodes.add(inner)
        }

        this.macs['CSMA'] = MyCSMA
        this.macs["SFAMA"] = SlottedFama
        this.macs["ALOHA"] = AlohaAN

    }

    LoadGenerator getGenerator(int node_number, float load){
        def l = new LoadGenerator(this.dest_nodes[node_number], load)
        return l
    }

    RouteAdder getAddder(int node_number){
        def r = new RouteAdder(this.routing_steps[node_number], this.address_list, this.routing_dist[node_number])
        return r
    }


    String getFileString(){
        return "BaseGridScenario"
    }
}

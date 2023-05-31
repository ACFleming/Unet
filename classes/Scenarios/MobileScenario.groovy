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
import MAC.*
// import SetupAgents.RouteAdder
import SetupAgents.*
// import SetupAgents.RouteAdder
import org.apache.commons.lang3.RandomUtils
import Scenarios.BaseScenario
import SetupAgents.TransportGenerator



class MobileScenario extends BaseScenario{

  

    MobileScenario(){
        super()


        this.setNodeCount(5)
        this.setNodeLocation([
            [0,  0, -10],
            [500,  0, -10],
            [1500, 0, -10],
            [2500,  0, -10],
            [0, 1145.92, -10],

        ])


        // this.setAddressList([])
        // this.setTransmitters([true, false, false, false, false])   
        this.transmittersSetAll()     
        this.generateAddrLists()
        this.dest_nodes = []
        this.motion_counters = [0,0,0,0,0]
        for(def n = 0; n < this.getNodeCount(); n++){
            def d = []
            // this.getAddressList
            if(n == 0 || n == 4){
                d = [this.getAddressList()[1],this.getAddressList()[2],this.getAddressList()[3]]
            }else{
                d = [this.getAddressList()[0], this.getAddressList()[4]]
            }
            dest_nodes.add(d)
        }
        print "Object detais: ${this.dump()}\n"


        this.setRoutingDist(routing_dist)
        this.setFileString("MobileScenario")
        print "Object detais: ${this.dump()}\n"

    }


    def getGenerator(int node_number, float load){
        // print "Transmitter status for ${this.address_list[node_number]}: ${this.transmitters[node_number]}\n"
        // def l = new Tran(this.dest_nodes[node_number], load, this.transmitters[node_number])
        def t = new SetupAgents.LoadGenerator(this.dest_nodes[node_number], load, this.transmitters[node_number])
        // print "DONE\n"
        return t
    }

    def getMotion(int node_number){
        def motion_model = {ts -> 
            def set_point = [speed: 0]
            return set_point
        }
        if(node_number == 0){
            def speed = 10.0
            def short_time = Math.sqrt(2*500*500)/speed
            def long_time = 2*short_time
            motion_model = { ts -> 
                def setpoint = [speed: speed]
                // print counter
                switch(this.getMotionCounters()[node_number]) {
                    case 0:
                        setpoint["heading"] = 45.deg
                        setpoint["duration"] = short_time
                        break;
                    case 1:
                        setpoint["heading"] = 135.deg
                        setpoint["duration"] = long_time
                        break;
                    case 2:
                        setpoint["heading"] = 45.deg
                        setpoint["duration"] = long_time
                        break;
                    case 3:
                        setpoint["heading"] = 135.deg
                        setpoint["duration"] = short_time
                        break;
                    case 4:
                        setpoint["heading"] = -135.deg
                        setpoint["duration"] = short_time
                        break;
                    case 5:
                        setpoint["heading"] = -45.deg
                        setpoint["duration"] = long_time
                        break;
                    case 6:
                        setpoint["heading"] = -135.deg
                        setpoint["duration"] = long_time
                        break;
                    case 7:
                        setpoint["heading"] = -45.deg
                        setpoint["duration"] = short_time
                        break;

                    default:
                        setpoint["turnRate"] = 50.dps
                    break
                }
                println setpoint
                this.setMotionCounterVal(node_number, (this.getMotionCounters()[0]+1)%8)
                return setpoint
            }
        }else if (node_number == 4){
            def speed2 = 20.0
            def straight_time = 2500/speed2
            def turn_time = 3600/speed2
            def turn_rate = speed2/20
            motion_model = { ts -> 
                def setpoint = [speed: speed2]
                // print counter
                switch(this.getMotionCounters()[4]) {
                    case 0:
                        setpoint["heading"] = 90.deg
                        setpoint["turnRate"] = 0.deg
                        setpoint["duration"] = straight_time
                        break;
                    case 1:
                        setpoint["turnRate"] = turn_rate
                        setpoint["duration"] = turn_time
                        break;
                    case 2:
                        setpoint["heading"] = -90.deg
                        setpoint["turnRate"] = 0.deg
                        setpoint["duration"] = straight_time
                        break;
                    case 3:
                        setpoint["turnRate"] = turn_rate
                        setpoint["duration"] = turn_time
                        break;

                    default:
                        setpoint["turnRate"] = 50.dps
                        break
                    }
                    println setpoint
                    this.setMotionCounterVal(node_number,  (this.getMotionCounters()[4]+1)%4)
                    return setpoint
                }
        }
        return motion_model

    }

}

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



class CollinearScenario extends BaseScenario{

  

    CollinearScenario(){
        super()


        this.setNodeCount(5)
        for(def i = 0; i < this.getNodeCount(); i++){
            this.setNodeLocationRow(i, [3000*i, 0, -10])
        }

        this.setTransmitters([true, false, false, false, true])        
        this.generateAddrLists()
        this.dest_nodes = []
        for(def n = 0; n < this.getNodeCount(); n++){
            def d = []
            
            if(n == 0){
                d.add(this.getAddressList(4))
            }
            if(n == 4){
                d.add(this.getAddressList(0))
            }
            dest_nodes.add(d)
        }
        print "Object detais: ${this.dump()}\n"

        def routing_steps = [
            [[],[1],[1],[1],[1]],
            [[0],[],[2],[2],[2]],
            [[1],[1],[],[3],[3]],
            [[2],[2],[2],[],[4]],
            [[3],[3],[3],[3],[]]
        ]
        this.setRoutingSteps(routing_steps)

        def routing_dist = [
            [0,1,2,3,4],
            [1,0,1,2,3],
            [2,1,0,1,2],
            [3,2,1,0,1],
            [4,3,2,1,0]
        ]
        this.setRoutingDist(routing_dist)

    }


    def getGenerator(int node_number, float load){
        print "Transmitter status for ${this.address_list[node_number]}: ${this.transmitters[node_number]}\n"
        // def l = new Tran(this.dest_nodes[node_number], load, this.transmitters[node_number])
        def t = new TransportGenerator(this.dest_nodes[node_number], load, this.transmitters[node_number])
        print "DONE\n"
        return t
    }


    String getFileString(){
        return "CollinearScenario"
    }
}

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
        this.setNodeLocation([])
        for(def i = 0; i < this.getNodeCount(); i++){
            this.setNodeLocationRow(i, [2100*i, 0, -10])
        }
        // this.setAddressList([])
        this.setTransmitters([true, false, false, false, false])        
        this.generateAddrLists()
        print "${this.getAddressList()}\n"
        this.dest_nodes = []
        for(def n = 0; n < this.getNodeCount(); n++){
            def d = []
            // this.getAddressList
            if(n == 0){
                d = [this.getAddressList()[4]]
            // }else if(n == 4){
            //     d = [this.getAddressList()[0]]
            }else{
                d.clear()
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
        this.setFileString("CollinearScenario")
        print "Object detais: ${this.dump()}\n"

    }


    def getGenerator(int node_number, float load){
        // print "Transmitter status for ${this.address_list[node_number]}: ${this.transmitters[node_number]}\n"
        // def l = new Tran(this.dest_nodes[node_number], load, this.transmitters[node_number])
        def t = new TransportGenerator(this.dest_nodes[node_number], load, this.transmitters[node_number])
        // print "DONE\n"
        return t
    }


}

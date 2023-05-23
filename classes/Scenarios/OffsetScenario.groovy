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
import org.apache.commons.lang3.RandomUtils



class OffsetScenario extends BaseScenario{

  

    OffsetScenario(){
        super()
        // print this.node_locations
        for(def i = 0; i < this.node_count; i++){
                        
            def theta = RandomUtils.nextFloat(0, 2*3.14159)
            def radius = RandomUtils.nextFloat(0,500)
            
            def x = this.node_locations[i][0]+radius*Math.cos(theta)
            def y = this.node_locations[i][1]+radius*Math.sin(theta)
            def z = this.node_locations[i][2]
            this.node_locations[i] = [x,y,z]

        }
        // print "${this.node_locations}\n"

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
        return "OffsetGridScenario"
    }
}

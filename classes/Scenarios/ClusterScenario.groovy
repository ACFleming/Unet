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



class ClusterScenario extends BaseScenario{

  

    ClusterScenario(){
        super()
        
        
        this.setNodeCount(8)
        for(def i = 0; i < this.getNodeCount(); i++){

            def theta = RandomUtils.nextFloat(0, 2*3.14159) 
            def radius = RandomUtils.nextFloat(100,1000)
            def x = 0
            def y = 0
            if(i < this.getNodeCount()/2){
                x = -1000 + radius*Math.cos(theta)
                y = -1000 + radius*Math.sin(theta)
            }else{
                x = 1000 + radius*Math.cos(theta)
                y = 1000 + radius*Math.sin(theta)
            }                 
            
            def z = this.getNodeLocationRow(0)[2]
            this.setNodeLocationRow(i, [x,y,z])

        }
        
        this.transmittersSetAll()
        this.generateAddrLists()
        this.dest_nodes = []
        for(def n = 0; n < this.getNodeCount(); n++){
            def d = []
            
            if(n < nodeCount/2){
                // println "${0..(nodeCount/2)-1}"
                d = address_list[0..<(nodeCount/2)-1]
            }else{
                // println 'b'
                d = address_list[(nodeCount/2)..(nodeCount-1)]
            }
            // d.remove([address_list[n]])
            d.remove(d.indexOf(address_list[n]))
            dest_nodes.add(d)
        }
        print "Object detais: ${this.dump()}\n"

        this.setFileString("ClusterScenario")

    }


}

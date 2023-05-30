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



class LowQuantityScenario extends BaseScenario{

  

    LowQuantityScenario(){
        super()
        
        
        this.setNodeCount(3)
        for(def i = 0; i < this.getNodeCount(); i++){

            def theta = RandomUtils.nextFloat(0, 2*3.14159) 
            def radius = RandomUtils.nextFloat(500,2500)                  
            def x = radius*Math.cos(theta)
            def y = radius*Math.sin(theta)
            
            def z = this.getNodeLocationRow(0)[2]
            this.setNodeLocationRow(i, [x,y,z])

        }
        
        // this.transmittersSetAll()
        this.setTransmitters([true, false,false])
        this.generateAddrLists()
        this.destNodesSetAll()
        this.setFileString("LowQuantityScenario")
        print "Object detais: ${this.dump()}\n"

    }

}

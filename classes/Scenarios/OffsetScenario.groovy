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



class OffsetScenario extends BaseScenario{

  

    OffsetScenario(){
        super()
        
        for(def i = 0; i < this.getNodeCount(); i++){
            
            def theta = RandomUtils.nextFloat(0, 2*3.14159)
            def radius = RandomUtils.nextFloat(100,300)
            def curr_loc = this.getNodeLocationRow(i)
            // print "THERE\n"

            def x = curr_loc[0]+radius*Math.cos(theta)
            def y = curr_loc[1]+radius*Math.sin(theta)
            def z = curr_loc[2]
            print "x ${x} y ${y} z ${z}\n"
            this.setNodeLocationRow(i, [x,y,z])

        }
        this.setFileString("OffsetGridScenario")
        print "Object detais: ${this.dump()}\n"
        
        

    }

}

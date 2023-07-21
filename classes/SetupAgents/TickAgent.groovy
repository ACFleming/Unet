package SetupAgents

import org.arl.fjage.Message
import org.arl.fjage.TickerBehavior
import org.codehaus.groovy.runtime.DateGroovyMethods
import org.arl.unet.*
import org.apache.commons.lang3.time.DateUtils
import java.text.SimpleDateFormat
import java.time.LocalDateTime


class TickAgent extends UnetAgent {

    double prev_sys_time;
    double prev_phy_time;
    void startup() {
        
        def phy = agentForService Services.PHYSICAL
        this.prev_sys_time = LocalDateTime.now().getSecond();
        this.prev_phy_time = phy.time/1000000
        // print "Sys time ${this.prev_sys_time} Phy time ${this.prev_phy_time}\n"
        // print "\n"
        add new TickerBehavior(1,{
            def curr_phy_time = phy.time/1000000
            def curr_sys_time = LocalDateTime.now().getSecond();
            if(curr_sys_time > this.prev_sys_time){
                print "Curr ${curr_phy_time} Prev ${this.prev_phy_time} Phy sec diff per 1 sec ${curr_phy_time - this.prev_phy_time}\n"
                this.prev_phy_time = curr_phy_time;
            }
            // def phy_diff = curr_phy_time - this.prev_phy_time;
            // def sys_diff = curr_sys_time - this.prev_sys_time;
            // def ratio = phy_diff/sys_diff;
            // print "Sys time ${this.prev_sys_time} Phy time ${this.prev_phy_time}\n"
            // print "Sys diff ${sys_diff} Phy diff ${phy_diff} FTRT Ratio ${ratio}\n"
            this.prev_sys_time = curr_sys_time;
            
            
        })
    }



}
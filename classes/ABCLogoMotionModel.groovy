import groovy.lang.Closure
Closure motion_model = { ts -> 
    def setpoint = [speed: 100.mps]
    def cycle_time = ts%108
    if(cycle_time == 0){
        setpoint['heading'] = 0.deg
    }else if (cycle_time <= 18.seconds){
        setpoint["turnRate"] = 10.dps
    }else if (cycle_time <= 36.seconds){
        setpoint["turnRate"] = -10.dps
    }else if (cycle_time <= 72.seconds){
        setpoint["turnRate"] = 10.dps
    }else if (cycle_time <= 90.seconds){
        setpoint["turnRate"] = -10.dps
    }else{
        setpoint["turnRate"] = 10.dps
    }
    return setpoint
}
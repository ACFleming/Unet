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



class EquidistantScenario extends BaseScenario{

  

    EquidistantScenario(){
        super()


        def loc = [
            [0.km, 1.732.km, -10.m],
            [-0.5.km, 0.866.km, -10.m],
            [0.5.km, 0.866.km, -10.m],
            [-1.km, 0.km, -10.m],
            [ 0.km,  0.km, -10.m],
            [1.km, 0.km, -10.m],
            [-0.5.km, -0.866.km, -10.m],
            [0.5.km, -0.866.km, -10.m],
            [0.km, -1.732.km, -10.m]        
        ]   
        for(def i = 0; i < this.getNodeCount(); i++){
            this.setNodeLocationRow(i, loc[i])
        }


        // this.setNodeCount(5)
        // for(def i = 0; i < this.getNodeCount(); i++){
        //     this.setNodeLocationRow(i, [3000*i, 0, -10])
        // }

        this.setTransmitters([
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            true,
        ])        
        this.generateAddrLists()
        this.dest_nodes = []
        for(def n = 0; n < this.getNodeCount(); n++){
            def d = []
            
            if(n == 0){
                d.add(this.getAddressList()[8])
            }
            if(n == 8){
                d.add(this.getAddressList()[0])
            }
            dest_nodes.add(d)
        }
        print "Object detais: ${this.dump()}\n"

        // def routing_steps = [
        //     [[]     ,[2]    ,[3]    ,[2]    ,[3,2]  ,[3]    ,[2,3]  ,[2,3]  ,[2,3]  ],
        //     [[1]    ,[]     ,[3]    ,[4]    ,[5]    ,[3,5]  ,[4,5]  ,[5]    ,[4,5]  ],
        //     [[1]    ,[2]    ,[]     ,[2,5]  ,[5]    ,[6]    ,[5]    ,[5,6]  ,[5,6]  ],
        //     [[2]    ,[2]    ,[2,5]  ,[]     ,[5]    ,[5]    ,[7]    ,[5,7]  ,[7]    ],
        //     [[2,3]  ,[2]    ,[3]    ,[4]    ,[]     ,[6]    ,[7]    ,[8]    ,[7,8]  ],
        //     [[3]    ,[3,5]  ,[3]    ,[5]    ,[5]    ,[]     ,[5,8]  ,[8]    ,[8]    ],
        //     [[4,5]  ,[4,5]  ,[5]    ,[4]    ,[5]    ,[5,8]  ,[]     ,[8]    ,[9]    ],
        //     [[5,6]  ,[5]    ,[5,6]  ,[5,7]  ,[5]    ,[6]    ,[7]    ,[]     ,[9]    ],
        //     [[7,8]  ,[7,8]  ,[7,8]  ,[7]    ,[7,8]  ,[8]    ,[7]    ,[8]    ,[]     ]
        // ]
        def routing_steps = [
            [[]     ,[1]    ,[2]    ,[1]    ,[1,2]  ,[2]    ,[1,2]  ,[1,2]  ,[1,2]  ],
            [[0]    ,[]     ,[2]    ,[3]    ,[4]    ,[2,4]  ,[3,4]  ,[3]    ,[3,4]  ],
            [[0]    ,[1]    ,[]     ,[1,4]  ,[4]    ,[5]    ,[4]    ,[4,5]  ,[4,5]  ],
            [[1]    ,[1]    ,[1,4]  ,[]     ,[4]    ,[4]    ,[6]    ,[4,6]  ,[6]    ],
            [[1,2]  ,[1]    ,[2]    ,[3]    ,[]     ,[5]    ,[6]    ,[7]    ,[6,7]  ],
            [[2]    ,[2,4]  ,[2]    ,[4]    ,[4]    ,[]     ,[4,7]  ,[7]    ,[7]    ],
            [[3,4]  ,[3,4]  ,[4]    ,[3]    ,[4]    ,[3,7]  ,[]     ,[7]    ,[8]    ],
            [[4,5]  ,[4]    ,[4,5]  ,[4,6]  ,[4]    ,[5]    ,[6]    ,[]     ,[8]    ],
            [[6,7]  ,[6,7]  ,[6,7]  ,[6]    ,[6,7]  ,[7]    ,[6]    ,[7]    ,[]     ]
        ]


        this.setRoutingSteps(routing_steps)

        def routing_dist = [
            [0,1,1,2,2,2,3,3,4],
            [1,0,1,1,1,2,2,2,3],
            [1,1,0,2,1,1,2,2,3],
            [2,1,2,0,1,2,1,2,2],
            [2,1,1,1,0,1,1,1,2],
            [2,2,1,2,1,0,2,1,2],
            [3,2,2,1,1,2,0,1,1],
            [3,2,2,2,1,1,1,0,1],
            [4,3,3,2,2,2,1,1,0]
        ]
        this.setRoutingDist(routing_dist)
        this.setFileString("EquidistantScenario")
    }


    def getGenerator(int node_number, float load){
        print "Transmitter status for ${this.address_list[node_number]}: ${this.transmitters[node_number]}\n"
        // def l = new Tran(this.dest_nodes[node_number], load, this.transmitters[node_number])
        def t = new TransportGenerator(this.dest_nodes[node_number], load, this.transmitters[node_number])
        print "DONE\n"
        return t
    }


}

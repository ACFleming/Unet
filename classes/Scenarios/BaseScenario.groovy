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




class BaseScenario{

    def T = 100.minutes  
    def load_range = [0.2, 1.0, 0.2] 

    def modem = [
            model:              org.arl.unet.sim.HalfDuplexModem,
            dataRate:           [2400.bps, 2400.bps],
            frameLength:        [32.bytes, 256.bytes],
            powerLevel:         [-10.dB, -10.dB],
            preambleDuration:   0,
            txDelay:            0,
            clockOffset:        0.s,
            headerLength:       0.s
            ]
    def channel = [
                    model:              org.arl.unet.sim.channels.ProtocolChannelModel,
                    soundSpeed:         1500.mps,
                    communicationRange: 3500.m,
                    interferenceRange:  3500.m,
                    detectionRange:     4000.m,
                    pDetection:         1,
                    pDecoding:          1
                ]
    def api_base = 1101
    def web_base = 8081
    def address_base = 10
    def api_list = []
    def web_list = []
    def address_list = []



    def macs = [:]


    def node_count = []
    def node_locations = []
    def transmitters = []
    def routing_steps = []
    def routing_dist = []
    def dest_nodes = []
    
    def file_string = "BaseGridScenario"

    BaseScenario(){

        this.node_count = 9
        this.node_locations = [
                [-1.km,  1.km, -10.m],
                [ 0.km,  1.km, -10.m],
                [ 1.km,  1.km, -10.m],
                [-1.km,  0.km, -10.m],
                [ 0.km,  0.km, -10.m],
                [ 1.km,  0.km, -10.m],
                [-1.km, -1.km, -10.m],
                [ 0.km, -1.km, -10.m],
                [ 1.km, -1.km, -10.m],
            ]


        this.transmittersSetAll()

        this.generateAddrLists()

        this.destNodesSetAll()

        this.macs['CSMA'] = MyCSMA
        this.macs["SFAMA"] = MySlottedFama
        this.macs["ALOHA"] = MyAlohaAN

        // for(int i = 0; i < this.getNodeCount(); i++){
        //     this.transmitters[i] = false
        // }

        // for(int i = 0; i < this.getNodeCount(); i++){
        //     this.dest_nodes[i] = [this.address_list[1]]
        // }


        print "Object detais: ${this.dump()}\n"

    }

    def getGenerator(int node_number, float load){
        // print "Transmitter status for ${this.address_list[node_number]}: ${this.transmitters[node_number]}\n"
        def l = new LoadGenerator(this.dest_nodes[node_number], load, this.transmitters[node_number])
        // print "DONE\n"
        return l
    }

    RouteAdder getAdder(int node_number){
        def r = new RouteAdder(this.routing_steps[node_number], this.address_list, this.routing_dist[node_number])
        return r
    }


    String setFileString(def file_string){
        this.file_string = file_string
    }

    String getFileString(){
        return this.file_string
    }

    void transmittersSetAll(){
        this.transmitters.clear()
        for(def i = 0; i < this.node_count; i++){
            this.transmitters.add(true)
        }
    }

    void generateAddrLists(){
        this.api_list.clear()
        this.web_list.clear()
        this.address_list.clear()
        for(def i = 0; i < this.node_count; i++){
            
            api_list.add(api_base+i)
            web_list.add(web_base+i)
            address_list.add(address_base+i)

        }
    }

    void destNodesSetAll(){
        this.dest_nodes.clear()
        for(def i = 0; i < this.node_count; i++){
            def inner = []
            for(def j = 0; j < this.node_count; j++){
                if(i!=j){
                    inner.add(address_list[j])
                }
            }
            this.dest_nodes.add(inner)
        }
    }


    public def getT() {
        return this.T;
    }

    public void setT(def T) {
        this.T = T;
    }

    public def getLoadRange() {
        return this.load_range;
    }

    public void setLoadRange(def load_range) {
        this.load_range = load_range;
    }

    public def getModem() {
        return this.modem;
    }

    public void setModem(def modem) {
        this.modem = modem;
    }

    public def getChannel() {
        return this.channel;
    }

    public void setChannel(def channel) {
        this.channel = channel;
    }

    public def getApiBase() {
        return this.api_base;
    }

    public void setApiBase(def api_base) {
        this.api_base = api_base;
    }

    public def getWebBase() {
        return this.web_base;
    }

    public void setWebBase(def web_base) {
        this.web_base = web_base;
    }

    public def getAddressBase() {
        return this.address_base;
    }

    public void setAddressBase(def address_base) {
        this.address_base = address_base;
    }

    public def getApiList() {
        return this.api_list;
    }

    public def getWebList() {
        return this.web_list;
    }

    public def getAddressList() {
        return this.address_list;
    }


    public void setAddressList(def address_list) {
        this.address_list = address_list;
    }


    public getMacType(def str){
        return this.macs[str]
    }

    public def getMacs() {
        return this.macs;
    }

    public void setMacs(def macs) {
        this.macs = macs;
    }

    public def getNodeCount() {
        return this.node_count;
    }

    public void setNodeCount(def node_count) {
        this.node_count = node_count;
    }

    public def getNodeLocations() {
        return this.node_locations;
    }

    public def getNodeLocationRow(def i) {
        return this.node_locations[i];
    }

    public void setNodeLocationRow(def i, def row) {
        print "${row}\n"
        this.node_locations[i] = row;
        print "${this.node_locations[i]}\n"
    }


    public void setNodeLocation(def node_locations) {
        this.node_locations = node_locations;
    }

    public def getTransmitters() {
        return this.transmitters;
    }

    public void setTransmitters(def transmitters) {
        this.transmitters = transmitters;
    }

    public def getRoutingSteps() {
        return this.routing_steps;
    }

    public void setRoutingSteps(def routing_steps) {
        this.routing_steps = routing_steps;
    }

    public def getRoutingDist() {
        return this.routing_dist;
    }

    public void setRoutingDist(def routing_dist) {
        this.routing_dist = routing_dist;
    }

    public def getDestNodes() {
        return this.dest_nodes;
    }

    public void setDestNodes(def dest_nodes) {
        this.dest_nodes = dest_nodes;
    }
    
}

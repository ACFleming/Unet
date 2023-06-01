import unetpy as ut
import fjagepy as fj
import time 
import sys


def main(args = None):
    pass
    
    socket = ut.UnetSocket('localhost', 1103)
    gateway = socket.getGateway()
    phy = gateway.agentForService(ut.Services.PHYSICAL)
    phy.refPowerLevel = 200
    phy[1].powerLevel = 0
    phy[2].powerLevel = 0
    phy[3].powerLevel = 0
    if(len(sys.argv) != 0):
        print(sys.argv)
        mode = str(sys.argv[1]).lower()
    else:
        mode = 'constant'
    if(mode == 'constant'):
        while(True):
            time.sleep(1)
            print("Sending LOUD")
            result = phy.send(ut.TxFrameReq(to=0, data=[11,11,11,11]))
    elif(mode == 'pulse'):
        for i in range(50):
            time.sleep(0.1)
            print("Sending LOUD")
            result = phy.send(ut.TxFrameReq(to=0, data=[11,11,11,11]))
   



if __name__ == "__main__":
    main()
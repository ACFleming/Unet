import unetpy as ut
import fjagepy as fj
import time
import sys


def main(args = None):
    pass
    
    socket = ut.UnetSocket('localhost', 1101)
    gateway = socket.getGateway()
    phy = gateway.agentForService(ut.Services.PHYSICAL)
    phy[1].powerLevel = -20
    phy[2].powerLevel = -20
    phy[3].powerLevel = -20
    if(len(sys.argv) != 0):
        print(sys.argv)
        mode = str(sys.argv[1]).lower()
    else:
        mode = 'constant'
    if(mode == 'constant'):
        while(True):
            time.sleep(1)
            print("Sending quiet")
            result = phy.send(ut.TxFrameReq(to=0, data=[20]))
    elif(mode == 'pulse'):
        for i in range(50):
            time.sleep(0.1)
            print("Sending quiet")
            result = phy.send(ut.TxFrameReq(to=0, data=[20]))
   



if __name__ == "__main__":
    main()
import unetpy as ut
import fjagepy as fj
import time
import sys
import numpy as np
# import arlpy.plot as plt
import matplotlib.pyplot as plt


def main(args = None):
    pass
    
    socket = ut.UnetSocket('localhost', 1104)
    gateway = socket.getGateway()
    phy = gateway.agentForService(ut.Services.PHYSICAL)
    bb = gateway.agentForService(ut.Services.BASEBAND)
    
    bb.request(ut.RecordBasebandSignalReq(recLength = 12000))
    rsp = gateway.receive(ut.RxBasebandSignalNtf, 2500)
    print(rsp)
    if(rsp == None):
        rsp = gateway.receive(100)
        print(rsp)
    else:
        # print(rsp.signal)
        sig = np.array(rsp.signal)
        # print(sig)
        # print(type(sig[0]))
        # print(sig[0].real)
        # print(rsp.signal[0].real)
        # print(sig.real)
        print(np.average(sig))
        print(np.std(sig))  
        # plt.plot(rsp.signal[:10000].real, fs=rsp.fs)
        plt.figure()
        plt.plot(sig.real, 'r')
        plt.plot(sig.imag, 'b')
        
        plt.show()




if __name__ == "__main__":
    main()
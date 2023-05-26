/******************************************************************************
Copyright (c) 2016, Pritish Nahar
This file is released under Simplified BSD License.
Go to http://www.opensource.org/licenses/BSD-3-Clause for full license details.
******************************************************************************/
package MAC

import org.arl.fjage.*
import org.arl.fjage.param.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*
import org.arl.unet.nodeinfo.*
/**
 * The class implements the Slotted FAMA protocol.
 * Reference:
 * Molins, Marcal, and Milica Stojanovic. "Slotted FAMA: a MAC protocol for underwater acoustic networks."
 * OCEANS 2006-Asia Pacific. IEEE, 2007.
 */
class MySlottedFama extends UnetAgent {

    ////// protocol constants

    private final static int PROTOCOL           = Protocol.MAC

    private final static int MAX_RETRY          = 3
    private final static int MAX_QUEUE_LEN      = 16
    private final static int MAX_BACKOFF_SLOTS  = 12
    private final static int MIN_BACKOFF_SLOTS  = 3

    private final static int AFFIRMATIVE        = 1
    private final static int NEGATIVE           = 0

    private final static int DATA_CHANNEL       = 1
    private final static int CONTROL_CHANNEL    = 0

    private final static int CONTINUE           = 1
    private final static int RESTART            = 2
    private final static int OFF                = 0
    private final static int ON                 = 1

    ////// PDU encoder/decoder
    private final static int RTS_PDU            = 0x01
    private final static int CTS_PDU            = 0x02
    private final static int DATA_PDU           = 0x03
    private final static int ACK_PDU            = 0x04
    private final static int NACK_PDU           = 0x05

    int startTime                               = 0
    int slotLength                              = 0
    int modemBusy                               = 0
    long backoffStartTime                       = 0
    long backoffEndTime                         = 0
    int senderOfXCTS                            = 0
    int senderOfXACK                            = 0
    int flagForCtsTimeout                       = OFF

    ArrayList<Integer> senderRTS = new ArrayList<Integer>()

    ////// reservation request queue
    private Queue<ReservationReq> queue = new ArrayDeque<ReservationReq>()

    def controlMsg = PDU.withFormat
    {
        uint8('type')
        uint16('duration')    // ms
    }

    ////// protocol FSM

    private enum State {
        IDLE, TX_RTS, TX_DATA, TX_CTS, TX_ACK, WAIT_FOR_CTS, WAIT_FOR_DATA, WAIT_FOR_ACK,
        BACKOFF_X_RTS, BACKOFF_X_CTS, BACKOFF_X_DATA, BACKOFF_CTS_TIMEOUT, BACKOFF_INTERFERENCE,
        RECEIVING,RECEIVING_FROM_BACKOFF_CTS_TIMEOUT
    }

    private enum Event {
        RX_RTS, RX_CTS, RX_ACK, RX_NACK, RX_DATA, SNOOP_RTS, SNOOP_CTS, SNOOP_DATA,SNOOP_ACK, SNOOP_NACK,
        RESERVATION_REQ, BADFRAME_NTF, CARRIER_SENSED, DATA_FRAME_CORRUPTED
    }

    private FSMBehavior fsm = FSMBuilder.build {

        int retryCount = 0
        int dataDetected = NEGATIVE
        int backoff = 0
        def rxInfo
        int ackTimeout = 0
        int waitTimeForCTS = 0
        int correspondent = 0
        int endTimeBackoffCtsTimeout = 0

        state(State.IDLE)
        {
            onEnter
            {
                long currentTime  = GetCurrentTime()

                // print "slotLength:"
                // print slotLength
                int timeForNextSlot = slotLength - ( (currentTime - startTime) % slotLength )
                // print "FAIL"
                // print 'IDLE\n'
                after(timeForNextSlot.milliseconds)
                {
                    // print 'CHECKING if modem is busy\n'
                    modemBusy = ModemCheck()
                    if (!modemBusy){
                        if(!queue.isEmpty())
                        {
                            // print "TX State\n"
                            setNextState(State.TX_RTS)
                        }
                    }else{
                        // print "RECEIVING\n"
                        setNextState(State.RECEIVING)
                    }
                }
            }

            action
            {
                block()
            }

            onEvent(Event.RESERVATION_REQ)
            {
                reenterState()
            }

            onEvent(Event.CARRIER_SENSED)
            {
                setNextState(State.RECEIVING)
            }

            onEvent(Event.BADFRAME_NTF){ waitTime ->
                backoff = waitTime
                setNextState(State.BACKOFF_INTERFERENCE)
            }
        }

        state(State.TX_RTS) {
            //Transmit an RTS packet
            onEnter
            {
                Message msg = queue.peek()
                // for(Message m: queue){
                //     print "Message : ${m}\n"
                // }
                print "${node.address} sending message: ${msg}\n"
                def bytes = controlMsg.encode(type: RTS_PDU, duration: Math.ceil(msg.duration*1000))
                phy << new ClearReq()
                phy << new TxFrameReq(to: msg.to, type: Physical.CONTROL, protocol: PROTOCOL, data: bytes)
                correspondent = msg.to
                after(controlMsgDuration.milliseconds)
                {
                    setNextState(State.WAIT_FOR_CTS)
                }
            }
        }

        state(State.WAIT_FOR_CTS)
        {
            //Wait for CTS till the end of the next slot.
            onEnter
            {
                print "${node.address} Waiting for CTS\n"
                long currentTime  = GetCurrentTime()
                waitTimeForCTS = slotLength - ( (currentTime - startTime) % slotLength ) + slotLength
                after(waitTimeForCTS.milliseconds)
                {
                    flagForCtsTimeout = ON
                    backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                    backoffStartTime = GetCurrentTime()
                    backoffEndTime = backoffStartTime + backoff
                    endTimeBackoffCtsTimeout = backoffEndTime
                    print "${node.address} CTS TIMEOUT\n"
                    setNextState(State.BACKOFF_CTS_TIMEOUT)
                }
            }

            onEvent(Event.RX_CTS)
            {
                
                setNextState(State.TX_DATA)
            }
        }

        state(State.TX_DATA)
        {
            //Transmit DATA packet.
            onEnter
            {
                long currentTime  = GetCurrentTime()
                int timeForNextSlot = slotLength - ( (currentTime - startTime ) % slotLength )
                after(timeForNextSlot.milliseconds)
                {
                    ReservationReq msg = queue.peek()
                    print "${node.address} SENDING AND WAITING FOR ACK\n"
                    sendReservationStatusNtf(msg, ReservationStatus.START)
                    after(msg.duration)
                    {
                        sendReservationStatusNtf(msg, ReservationStatus.END)
                        setNextState(State.WAIT_FOR_ACK)
                    }
                }
            }
        }

        state(State.WAIT_FOR_ACK)
        {
            //Wait for ACK for a time equal to ackTimeout as defined below.
            onEnter
            {
                long currentTime = GetCurrentTime()
                ackTimeout = 3*slotLength - ((currentTime-startTime)%slotLength)
                after(ackTimeout.milliseconds)
                {
                    if (++retryCount >= MAX_RETRY)
                    {
                        sendReservationStatusNtf(queue.poll(), ReservationStatus.FAILURE)
                        retryCount = 0
                        print "${node.address} ACK TIMEOUT\n"
                        setNextState(State.IDLE)
                    }
                    else
                    {
                        setNextState(State.TX_DATA)
                    }
                }
            }

            onEvent(Event.RX_ACK)
            {
                queue.poll()
                setNextState(State.IDLE)
            }

            onEvent(Event.RX_NACK)
            {
                if (++retryCount >= MAX_RETRY)
                {
                    sendReservationStatusNtf(queue.poll(), ReservationStatus.FAILURE)
                    retryCount = 0
                    setNextState(State.IDLE)
                }
                else
                {
                    setNextState(State.TX_DATA)
                }
            }
        }

        state(State.TX_CTS)
        {
            //Transmit a CTS packet.
            onEnter
            {
                int destination = senderRTS.get(new Random().nextInt(senderRTS.size()))
                senderRTS.clear()
                def bytes = controlMsg.encode(type: CTS_PDU, duration: Math.round(rxInfo.duration*1000))
                phy << new ClearReq()
                phy << new TxFrameReq(to: destination, type: Physical.CONTROL, protocol: PROTOCOL, data: bytes)
                rxInfo = null
                after(controlMsgDuration.milliseconds)
                {
                    setNextState(State.WAIT_FOR_DATA)
                }
            }
        }

        state(State.WAIT_FOR_DATA)
        {
            //Wait for a DATA packet.
            onEnter
            {
                long currentTime = GetCurrentTime()
                int waitTimeForData = 2*slotLength-((currentTime-startTime)%slotLength)
                after(waitTimeForData.milliseconds)
                {
                    if(dataDetected == NEGATIVE)
                    {
                        backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                        backoffStartTime = GetCurrentTime()
                        backoffEndTime = backoffStartTime + backoff
                        setNextState(State.IDLE)
                    }
                    dataDetected = NEGATIVE
                }
            }

            onEvent(Event.RX_DATA){ info ->
                long currentTime  = GetCurrentTime()
                int timeForNextSlot = slotLength - ( (currentTime - startTime ) % slotLength )
                rxInfo = info
                after(timeForNextSlot.milliseconds)
                {
                    setNextState(State.TX_ACK)
                }
            }

            onEvent(Event.CARRIER_SENSED){channelType->
                if(channelType == DATA_CHANNEL)
                {
                    dataDetected = AFFIRMATIVE
                }
            }

            onEvent(Event.DATA_FRAME_CORRUPTED)
            {
                dataDetected = NEGATIVE
                long currentTime = GetCurrentTime()
                int timeForNextSlot = slotLength - ( (currentTime - startTime ) % slotLength )
                after(timeForNextSlot.milliseconds)
                {
                    def bytes = controlMsg.encode(type: NACK_PDU, duration: dataMsgDuration)
                    phy << new ClearReq()
                    phy << new TxFrameReq(to: correspondent, type: Physical.CONTROL, protocol: PROTOCOL, data: bytes)
                    reenterState()
                }
            }
        }

        state(State.TX_ACK)
        {
            //Transmit an ACK packet
            onEnter
            {
                if(rxInfo.duration == null){
                    rxInfo.duration = slotLength
                }
                def bytes = controlMsg.encode(type: ACK_PDU, duration: Math.round(rxInfo.duration))
                phy << new ClearReq()
                phy << new TxFrameReq(to: rxInfo.from, type: Physical.CONTROL, protocol: PROTOCOL, data: bytes)
                rxInfo = null
                after(controlMsgDuration.milliseconds)
                {
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                        if(flagForCtsTimeout == ON)
                        {
                            backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            endTimeBackoffCtsTimeout = backoffEndTime
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                }
            }
        }

        state(State.BACKOFF_X_RTS)
        {
            //Backoff due to having received an X_RTS packet.
            onEnter
            {
                after(backoff.milliseconds)
                {
                    modemBusy = ModemCheck()
                    if(modemBusy)
                    {
                        setNextState(State.RECEIVING)
                    }
                    else
                    {
                        if(timerCtsTimeoutOpMode == CONTINUE)
                        {
                            if(endTimeBackoffCtsTimeout > GetCurrentTime())
                            {
                                backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                        }
                        if(timerCtsTimeoutOpMode == RESTART)
                        {
                            if(flagForCtsTimeout == ON)
                            {
                                backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                endTimeBackoffCtsTimeout = backoffEndTime
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                        }
                    }
                }
            }
        }

        state(State.BACKOFF_X_CTS)
        {
            //Backoff due to having received an X_CTS packet.
            onEnter
            {
                after(backoff.milliseconds)
                {
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                        if(flagForCtsTimeout == ON)
                        {
                            backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            endTimeBackoffCtsTimeout = backoffEndTime
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                }
            }

            onEvent(Event.SNOOP_ACK) { info ->
                senderOfXACK = info.from
                if(senderOfXCTS == senderOfXACK || senderOfXCTS == 0)
                {
                    senderOfXCTS = 0
                    senderOfXACK = 0
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                        if(flagForCtsTimeout == ON)
                        {
                            backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            endTimeBackoffCtsTimeout = backoffEndTime
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                }
                else
                {
                    backoffStartTime = GetCurrentTime()
                    backoff = backoffEndTime - backoffStartTime
                    setNextState(State.BACKOFF_X_CTS)
                }
            }

            onEvent(Event.SNOOP_NACK) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }
        }

        state(State.BACKOFF_X_DATA)
        {
            //Backoff due to having received an X_DATA packet.
            onEnter
            {
                after(backoff.milliseconds)
                {
                    backoffEndTime = GetCurrentTime()+slotLength
                    after(slotLength.milliseconds)
                    {
                        if(timerCtsTimeoutOpMode == CONTINUE)
                        {
                            if(endTimeBackoffCtsTimeout > GetCurrentTime())
                            {
                                backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                        }
                        if(timerCtsTimeoutOpMode == RESTART)
                        {
                            if(flagForCtsTimeout == ON)
                            {
                                backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                endTimeBackoffCtsTimeout = backoffEndTime
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                        }
                    }
                }
            }

            onEvent(Event.CARRIER_SENSED){channelType->
                if(channelType == DATA_CHANNEL)
                {
                    long currentTime = GetCurrentTime()
                    backoff = dataMsgDuration+maxPropagationDelay-((currentTime-startTime)%slotLength)+2*slotLength - ((currentTime-((currentTime-startTime)%slotLength)+dataMsgDuration+maxPropagationDelay)%slotLength)
                    backoffStartTime = GetCurrentTime()
                    backoffEndTime = backoffStartTime + backoff
                    reenterState()
                }
            }

            onEvent(Event.SNOOP_ACK) { info ->
                senderOfXACK = info.from
                if(senderOfXCTS == senderOfXACK || senderOfXCTS == 0)
                {
                    senderOfXCTS = 0
                    senderOfXACK = 0
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                            if(flagForCtsTimeout == ON)
                            {
                                backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                endTimeBackoffCtsTimeout = backoffEndTime
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                    }
                }
                else
                {
                    backoffStartTime = GetCurrentTime()
                    backoff = backoffEndTime - backoffStartTime
                    setNextState(State.BACKOFF_X_DATA)
                }
            }

            onEvent(Event.SNOOP_NACK) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

        }

        state(State.BACKOFF_CTS_TIMEOUT) {
            //Backoff due to a CTS Timeout.
            onEnter
            {
                after(backoff.milliseconds)
                {
                    flagForCtsTimeout = OFF
                    setNextState(State.IDLE)
                }
            }

            onEvent(Event.CARRIER_SENSED)
            {
                setNextState(State.RECEIVING_FROM_BACKOFF_CTS_TIMEOUT)
            }
        }

        state(State.BACKOFF_INTERFERENCE)
        {
            //Backoff due to having received sensed interference(having received a BadFrameNtf).
            onEnter
            {
                after(backoff.milliseconds)
                {
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                        if(flagForCtsTimeout == ON)
                        {
                            backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                }
            }
        }

        state(State.RECEIVING)
        {
            //Enter this state when you sense carrier in the IDLE state or after backoff timer expires in BACKOFF_X_RTS state.
            onEvent(Event.RX_RTS){info ->
                

                rxInfo = info
                senderRTS.add(rxInfo.from)
                print "${node.address} sending CTS to ${info.from} at next slot\n"
                long currentTime = GetCurrentTime()
                long delayForTX_CTS = slotLength - ( (currentTime - startTime) % slotLength )
                correspondent = info.from
                after(delayForTX_CTS.milliseconds)
                {
                    
                    setNextState(State.TX_CTS)
                }
            }
            onEvent(Event.RX_CTS)
            {
                setNextState(State.TX_DATA)
            }

            onEvent(Event.SNOOP_RTS) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_RTS)
            }

            onEvent(Event.SNOOP_CTS) { info ->
                senderOfXCTS = info.from
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

            onEvent(Event.SNOOP_DATA) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_DATA)
            }

            onEvent(Event.SNOOP_ACK) { info ->
                setNextState(State.IDLE)
            }

            onEvent(Event.SNOOP_NACK) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

            onEvent(Event.BADFRAME_NTF){ waitTime ->
                backoff = waitTime
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_INTERFERENCE)
            }
        }

        state(State.RECEIVING_FROM_BACKOFF_CTS_TIMEOUT)
        {
            //Enter this state when you sense carrier in the BACKOFF_CTS_TIMEOUT state.
            onEvent(Event.RX_RTS){info ->
                rxInfo = info
                senderRTS.add(rxInfo.from)
                long currentTime = GetCurrentTime()
                long delayForTX_CTS = slotLength - ( (currentTime - startTime) % slotLength )
                correspondent = info.from
                after(delayForTX_CTS.milliseconds)
                {
                    setNextState(State.TX_CTS)
                }
            }

            onEvent(Event.RX_CTS)
            {
                setNextState(State.TX_DATA)
            }

            onEvent(Event.SNOOP_RTS) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_RTS)
            }

            onEvent(Event.SNOOP_CTS) { info ->
                senderOfXCTS = info.from
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

            onEvent(Event.SNOOP_DATA) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_DATA)
            }

            onEvent(Event.SNOOP_ACK) { info ->
                if(timerCtsTimeoutOpMode == CONTINUE)
                {
                    if(endTimeBackoffCtsTimeout > GetCurrentTime())
                    {
                        backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                        backoffStartTime = GetCurrentTime()
                        backoffEndTime = backoffStartTime + backoff
                        setNextState(State.BACKOFF_CTS_TIMEOUT)
                    }
                    else
                    {
                        setNextState(State.IDLE)
                    }
                }
                if(timerCtsTimeoutOpMode == RESTART)
                {
                    if(flagForCtsTimeout == ON)
                    {
                        backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                        backoffStartTime = GetCurrentTime()
                        backoffEndTime = backoffStartTime + backoff
                        setNextState(State.BACKOFF_CTS_TIMEOUT)
                    }
                    else
                    {
                        setNextState(State.IDLE)
                    }
                }
            }

            onEvent(Event.SNOOP_NACK) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

            onEvent(Event.BADFRAME_NTF){ waitTime ->
                backoff = waitTime
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_INTERFERENCE)
            }
        }

    } // of FSMBuilder

  ////// agent startup sequence

    private AgentID phy
    private AgentID node
    private int addr

    @Override
    void setup()
    {
        register Services.MAC
    }

    private double dist(n1, n2){
        def sqr_sum = 0
        for(int i = 0; i < n1.size(); i++){
            sqr_sum +=(n1[i]-n2[i])*(n1[i]-n2[i])
        }
        return Math.sqrt(sqr_sum)
    }


    public void initParams(address_list, List node_locations, channel, modem){
        // print "INIT"
        def nodes = []
        def nodeCount = address_list.size()
        
        // print node_locations.size()
        for(int i = 0; i<nodeCount; i++){
            nodes.add(i)
        }
        def sum = 0
        def n = 0
        def maxPropagationDelay = 0
        // def propagationDelay = new ArrayList<ArrayList<Integer>>();

        for(int n1 = 0; n1 < nodeCount; n1++){
            
            // def row = new ArrayList<Integer>()

            for(int n2 = 0; n2 < nodeCount; n2++){
                def dist = this.dist(node_locations[n1], node_locations[n2])
                def delay = (int) (dist * 1000) / channel.soundSpeed + 0.5
                if( delay > maxPropagationDelay){
                    maxPropagationDelay = delay
                }
                // row.add(delay)
                
                
            }
            
            // propagationDelay.add(row)
        }


        this.timerCtsTimeoutOpMode = 2
        this.maxPropagationDelay = maxPropagationDelay
        // print "Max prop"
        // print this.maxPropagationDelay
        // print "\n"

        assert modem.dataRate[0] != 0
        assert modem.dataRate[1] != 0
        this.dataMsgDuration = (int)(8000*modem.frameLength[1]/modem.dataRate[1] + 0.5)
        this.setControlMsgDuration((int)(8000*modem.frameLength[0]/modem.dataRate[0] + 0.5))
        this.slotLength = controlMsgDuration + maxPropagationDelay + 1
        print "Slot length: ${this.slotLength}\n"
        // print "SL\n"

    }

    @Override
    void startup()
    {
        phy = agentForService(Services.PHYSICAL)
        node = agentForService(Services.NODE_INFO)

        subscribe phy
        subscribe(topic(phy, Physical.SNOOP))

        startTime = 0
        print "${this.node.Address}\n"
        // addr = node.Address
        addr = node.Address
        // assert 7 == 6
        // print "FSM"
        
        add(fsm)

    }

  ////// process MAC service requests

    private void setControlMsgDuration(int controlMsgDuration)
    {
        this.controlMsgDuration = controlMsgDuration
        initilialisationPhase()
    }

    private void initilialisationPhase()
    {
        slotLength = controlMsgDuration + maxPropagationDelay + 1
    }

    private long GetCurrentTime()
    {
        ParameterReq req = new ParameterReq(agentForService(Services.PHYSICAL))
        req.get(PhysicalParam.time)
        ParameterRsp rsp = (ParameterRsp)request(req, 1000)
        long time = rsp.get(PhysicalParam.time)
        time = time / 1000
        return time
    }

    private int ModemCheck()
    {
        if(phy.busy){
            return 1
        }
        return 0
        
    }

    @Override
    Message processRequest(Message msg)
    {
        // print "{Received request in SFAMA: ${msg}\n"
        switch (msg)
        {
            case ReservationReq:
                if (msg.to == Address.BROADCAST || msg.to == addr) return new Message(msg, Performative.REFUSE)
                if (msg.duration <= 0 || msg.duration > maxReservationDuration) return new Message(msg, Performative.REFUSE)
                if (queue.size() >= MAX_QUEUE_LEN) return new Message(msg, Performative.REFUSE)
                queue.add(msg)
                fsm.trigger(Event.RESERVATION_REQ)
                // print "GOT REQUEST\n"
                print "${node.address} requesting to send to ${msg.to}\n"
                return new ReservationRsp(msg)
            case ReservationCancelReq:
            case ReservationAcceptReq:
            case TxAckReq:
                return new Message(msg, Performative.REFUSE)
        }
        return null
    }

    @Override
    void processMessage(Message msg)
    {
        // print "${addr} received ${msg}\n"
        if (msg instanceof RxFrameNtf)
        {
            def rx = controlMsg.decode(msg.data)
            // print "RX:${rx}\n"
            long currentTime = GetCurrentTime()
            int timeForNextSlot = slotLength - ( (currentTime - startTime ) % slotLength )
            def info = [from: msg.from, to: msg.to, duration: slotLength]

            if(msg.type == Physical.CONTROL)
            {
                /*Check for type of Control packet received and it's intended receiver. If current node is not the
                intended receiver, set appropriate backoff-times and trigger events corresponding to the packet received. */

                if (rx.type == RTS_PDU)
                {
                    if(info.to == addr)
                    {
                        print "         ${node.address} RECEIVED RTS\n"
                        info.duration = rx.duration
                        fsm.trigger(Event.RX_RTS, info)
                    }
                    else
                    {
                    print "                     ${node.address} SNOOP RTS\n"
                        info.duration = 2*slotLength
                        fsm.trigger(Event.SNOOP_RTS, info)
                    }
                }
                else if (rx.type == CTS_PDU)
                {
                    
                    if(info.to == addr)
                    {
                        print "         ${node.address} RECEIVED CTS\n"
                        fsm.trigger(Event.RX_CTS)
                    }
                    else
                    {
                        print "                 ${node.address} SNOOP CTS\n"
                        info.duration = timeForNextSlot+dataMsgDuration+maxPropagationDelay+2*slotLength - ((currentTime-startTime+timeForNextSlot+dataMsgDuration+maxPropagationDelay)%slotLength)
                        fsm.trigger(Event.SNOOP_CTS, info)
                    }
                }
                else if (rx.type == ACK_PDU)
                {
                    if(info.to == addr)
                    {
                        print "         ${node.address} RECEIVED ACK\n"
                        fsm.trigger(Event.RX_ACK, info)
                    }
                    else
                    {
                        print "                 ${node.address} SNOOP ACK\n"
                        fsm.trigger(Event.SNOOP_ACK, info)
                    }
                }
                else if (rx.type == NACK_PDU)
                {
                    if(info.to == addr)
                    {
                        print "         ${node.address} RECEIVED NACK\n"
                        fsm.trigger(Event.RX_NACK, info)
                    }
                    else
                    {
                        print "                 ${node.address} SNOOP NACK\n"
                        info.duration = timeForNextSlot+dataMsgDuration+maxPropagationDelay+2*slotLength - ((currentTime-startTime+timeForNextSlot+dataMsgDuration+maxPropagationDelay-startTime)%slotLength)
                        fsm.trigger(Event.SNOOP_NACK, info)
                    }
                }
                else
                {
                  //pass
                }
            }

            if(msg.type == Physical.DATA)
            {
                /*If DATA packet is intended for current node, trigger event RX_DATA.
                Else set appropriate backoff time and trigger event SNOOP_DATA */

                if(info.to == addr)
                {
                    info.duration = get(phy, Physical.CONTROL, PhysicalChannelParam.frameDuration)
                    info.from     = msg.getFrom()
                    print "${node.address} RECEIVED DATA\n"
                    fsm.trigger(Event.RX_DATA, info)
                }
                else
                {
                    info.duration = timeForNextSlot + slotLength
                    if(backoffEndTime - info.duration - GetCurrentTime() == slotLength)
                    {
                        info.duration += slotLength
                    }
                    fsm.trigger(Event.SNOOP_DATA, info)
                }
            }
        }

        if(msg instanceof BadFrameNtf || msg instanceof CollisionNtf)
        {
            /*If node receives BadFrameNtf in the state WAIT_FOR_DATA, trigger Event DATA_FRAME_CORRUPTED.
            Else trigger event BADFRAME_NTF. */
            def currentTime = GetCurrentTime()
            def timeForNextSlot = slotLength - ((GetCurrentTime()-startTime)%slotLength)
            def backoff = timeForNextSlot+dataMsgDuration+maxPropagationDelay+2*slotLength - ((currentTime-startTime+timeForNextSlot+dataMsgDuration+maxPropagationDelay)%slotLength)

            if(fsm.getCurrentState().toString() == "WAIT_FOR_DATA")
            {
                fsm.trigger(Event.DATA_FRAME_CORRUPTED)
            }
            else
            {
                println "BADFRAME\n"
                fsm.trigger(Event.BADFRAME_NTF, backoff)
            }
        }

        if(msg instanceof RxFrameStartNtf)
        {
            //This notification denotes carrier sense and hence the corresponding event is triggered.
            fsm.trigger(Event.CARRIER_SENSED,msg.type)
        }
    }

    ////// expose parameters that are expected of a MAC service

    final int reservationPayloadSize = 0            // read-only parameters
    final int ackPayloadSize = 0
    final float maxReservationDuration = 65.535


    //Parameters to be passed to Agent File
    int controlMsgDuration, dataMsgDuration, maxPropagationDelay, timerCtsTimeoutOpMode

    @Override
    List<Parameter> getParameterList() {
        allOf(SlottedFamaParam,MacParam)
    }

    boolean getChannelBusy() {                      // channel is considered busy if fsm is not IDLE
        return fsm.currentState.name != State.IDLE
    }

    float getRecommendedReservationDuration() {     // recommended reservation duration is one DATA packet
        return get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
    }

  ////// utility methods

    private void sendReservationStatusNtf(ReservationReq msg, ReservationStatus status) {
        send new ReservationStatusNtf(recipient: msg.sender, inReplyTo: msg.msgID, to: msg.to, from: addr, status: status)
    }

}

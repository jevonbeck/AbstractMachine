package org.ricts.abstractmachine.components.compute.cu.fsm

import org.ricts.abstractmachine.components.interfaces.ComputeCore
import org.ricts.abstractmachine.components.interfaces.DecoderUnit
import org.ricts.abstractmachine.components.interfaces.FetchCore
import spock.lang.Specification
import spock.lang.Unroll

class ControlUnitFSMTest extends Specification {

    ControlUnitFSM testObj

    FetchCore fetchCoreMock = Mock()
    ComputeCore computeCoreMock = Mock()
    DecoderUnit decoderMock = Mock()

    static String fetchState = ControlUnitState.GenericCUState.FETCH.name()
    static String decodeState = ControlUnitState.GenericCUState.DECODE.name()
    static String executeState = ControlUnitState.GenericCUState.EXECUTE.name()
    static String haltState = ControlUnitState.GenericCUState.HALT.name()
    static String sleepState = ControlUnitState.GenericCUState.SLEEP.name()

    def setup() {
        computeCoreMock.getDecoderUnit() >> decoderMock

        testObj = new ControlUnitFSM(fetchCoreMock, computeCoreMock)
    }

    def "Initial state after reset should be fetch"(){
        when:
        testObj.reset()

        then:
        testObj.isInFetchState()
        testObj.currentState() == fetchState
    }

    def "Should return to fetch state whenever reset invoked"(){
        given:
        testObj.reset()

        when:
        testObj.triggerStateChange()
        testObj.reset()

        then:
        testObj.isInFetchState()
        testObj.currentState() == fetchState

        when:
        testObj.triggerStateChange()
        testObj.triggerStateChange()
        testObj.reset()

        then:
        testObj.isInFetchState()
        testObj.currentState() == fetchState
    }

    def "Should follow Fetch-Decode-Execute cycle"(){
        given:
        testObj.reset()

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInDecodeState()
        testObj.currentState() == decodeState

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInExecuteState()
        testObj.currentState() == executeState

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInFetchState()
        testObj.currentState() == fetchState

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInDecodeState()
        testObj.currentState() == decodeState

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInExecuteState()
        testObj.currentState() == executeState

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInFetchState()
        testObj.currentState() == fetchState
    }

    @Unroll
    def "Should break Fetch-Decode-Execute cycle if next state set"(){
        given:
        testObj.reset()
        testObj.setNextState(desiredState)

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInFetchState() == isFetchState
        testObj.isInDecodeState() == isDecodeState
        testObj.isInExecuteState() == isExecuteState
        testObj.isInHaltState() == isHaltState
        testObj.isInSleepState() == isSleepState
        testObj.currentState() == desiredState

        where:
        desiredState | isFetchState | isDecodeState | isExecuteState | isHaltState | isSleepState
        fetchState   | true         | false         | false          | false       | false
        decodeState  | false        | true          | false          | false       | false
        executeState | false        | false         | true           | false       | false
        haltState    | false        | false         | false          | true        | false
        sleepState   | false        | false         | false          | false       | true
    }

    @Unroll
    def "Should go to desired current state when valid state invoked"(){
        when:
        testObj.setCurrentState(desiredState)

        then:
        testObj.isInFetchState() == isFetchState
        testObj.isInDecodeState() == isDecodeState
        testObj.isInExecuteState() == isExecuteState
        testObj.isInHaltState() == isHaltState
        testObj.isInSleepState() == isSleepState
        testObj.currentState() == desiredState

        where:
        desiredState | isFetchState | isDecodeState | isExecuteState | isHaltState | isSleepState
        fetchState   | true         | false         | false          | false       | false
        decodeState  | false        | true          | false          | false       | false
        executeState | false        | false         | true           | false       | false
        haltState    | false        | false         | false          | true        | false
        sleepState   | false        | false         | false          | false       | true
    }

    @Unroll
    def "Should stay in #initialState state when set and state triggered"(){
        given:
        testObj.setCurrentState(initialState)

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInHaltState() == isHaltState
        testObj.isInSleepState() == isSleepState
        testObj.currentState() == initialState

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInHaltState() == isHaltState
        testObj.isInSleepState() == isSleepState
        testObj.currentState() == initialState

        where:
        initialState | isHaltState | isSleepState
        haltState    | true        | false
        sleepState   | false       | true
    }

    @Unroll
    def "Should go to #expectedNextState when initialised to #initialState and state triggered"(){
        given:
        testObj.setCurrentState(initialState)

        when:
        testObj.triggerStateChange()

        then:
        testObj.currentState() == expectedNextState

        where:
        initialState | expectedNextState
        fetchState   | decodeState
        decodeState  | executeState
        executeState | fetchState
        haltState    | haltState
        sleepState   | sleepState
    }
}

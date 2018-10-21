package org.ricts.abstractmachine.components.compute.cu.fsm

import org.ricts.abstractmachine.components.interfaces.ComputeCore
import org.ricts.abstractmachine.components.interfaces.DecoderUnit
import org.ricts.abstractmachine.components.interfaces.FetchCore
import spock.lang.Specification
import spock.lang.Unroll

class PipelinedControlUnitFSMTest extends Specification {

    PipelinedControlUnitFSM testObj

    FetchCore fetchCoreMock = Mock()
    ComputeCore computeCoreMock = Mock()
    DecoderUnit decoderMock = Mock()

    static String activeState = ControlUnitState.GenericCUState.ACTIVE.name()
    static String haltState = ControlUnitState.GenericCUState.HALT.name()
    static String sleepState = ControlUnitState.GenericCUState.SLEEP.name()

    def setup() {
        computeCoreMock.getDecoderUnit() >> decoderMock

        testObj = new PipelinedControlUnitFSM(fetchCoreMock, computeCoreMock)
    }

    def "Initial state after reset should be active"(){
        when:
        testObj.reset()

        then:
        testObj.isInActiveState()
        testObj.currentState() == activeState
    }

    @Unroll
    def "Should follow pipelined execution"(){
        given:
        testObj.reset()
        fetchCoreMock.getInstructionPC() >>> [0, pc1, pc2]
        fetchCoreMock.getIR() >>> [0, ir1, ir2]
        decoderMock.isValidInstruction() >>> [false, true, true]
        decoderMock.getProgramCounter() >>> [pc1, pc2]
        decoderMock.getMneumonic() >>> [mneumonic1, mneumonic2]
        decoderMock.getOperands() >>> [operands1, operands2]

        when:
        testObj.triggerStateChange()

        then:
        1 * fetchCoreMock.fetchInstruction()
        1 * decoderMock.decode(0, 0)
        0 * computeCoreMock.executeInstruction(_, _, _)

        when:
        testObj.triggerStateChange()

        then:
        1 * fetchCoreMock.fetchInstruction()
        1 * decoderMock.decode(pc1, ir1)
        1 * computeCoreMock.executeInstruction(pc1, mneumonic1, operands1)

        when:
        testObj.triggerStateChange()

        then:
        1 * fetchCoreMock.fetchInstruction()
        1 * decoderMock.decode(pc2, ir2)
        1 * computeCoreMock.executeInstruction(pc2, mneumonic2, operands2)

        where:
        pc1 | ir1 | mneumonic1 | operands1 | pc2 | ir2 | mneumonic2 | operands2
        1   | 5   | "JUMP"     | [10]      | 10  | 27  | "NOP"      | []
        12  | 7   | "BLAH"     | [10,5]    | 13  | 67  | "BLAH2"    | [5,43,1]
    }

    @Unroll
    def "Should break normal cycle if next state set"(){
        given:
        testObj.reset()
        testObj.setNextState(desiredState)

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInActiveState() == isActiveState
        testObj.isInHaltState() == isHaltState
        testObj.isInSleepState() == isSleepState
        testObj.currentState() == desiredState

        where:
        desiredState | isActiveState | isHaltState | isSleepState
        activeState  | true          | false       | false
        haltState    | false         | true        | false
        sleepState   | false         | false       | true
    }

    @Unroll
    def "Should go to desired current state when valid state invoked"(){
        when:
        testObj.setCurrentState(desiredState)

        then:
        testObj.isInActiveState() == isActiveState
        testObj.isInHaltState() == isHaltState
        testObj.isInSleepState() == isSleepState
        testObj.currentState() == desiredState

        where:
        desiredState | isActiveState | isHaltState | isSleepState
        activeState  | true          | false       | false
        haltState    | false         | true        | false
        sleepState   | false         | false       | true
    }

    @Unroll
    def "Should stay in #initialState state when set and state triggered"(){
        given:
        testObj.setCurrentState(initialState)

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInActiveState() == isActiveState
        testObj.isInHaltState() == isHaltState
        testObj.isInSleepState() == isSleepState
        testObj.currentState() == initialState

        when:
        testObj.triggerStateChange()

        then:
        testObj.isInActiveState() == isActiveState
        testObj.isInHaltState() == isHaltState
        testObj.isInSleepState() == isSleepState
        testObj.currentState() == initialState

        where:
        initialState | isActiveState | isHaltState | isSleepState
        activeState  | true          | false       | false
        haltState    | false         | true        | false
        sleepState   | false         | false       | true
    }
}

package org.ricts.abstractmachine.components.compute.cu.fsm

import org.ricts.abstractmachine.components.interfaces.ComputeCore
import org.ricts.abstractmachine.components.interfaces.DecoderUnit
import spock.lang.Specification
import spock.lang.Unroll

class ControlUnitExecuteStateTest extends Specification {

    ControlUnitExecuteState testObj

    ComputeCore coreMock = Mock()
    DecoderUnit decoderMock = Mock()

    def setup(){
        testObj = new ControlUnitExecuteState(coreMock, decoderMock)
    }

    @Unroll
    def "Should execute instruction when decoder says its valid, otherwise check interrupts only"(){
        given:
        int pc = 10
        String mneumonic = "INSTR"
        int [] operands = [2,3,0]
        decoderMock.getProgramCounter() >> pc
        decoderMock.getMneumonic() >> mneumonic
        decoderMock.getOperands() >> operands
        decoderMock.isValidInstruction() >> isValidInstruction

        when:
        testObj.performAction()

        then:
        m * coreMock.executeInstruction(pc, mneumonic, operands)
        n * coreMock.checkInterrupts()

        where:
        isValidInstruction | m | n
        true               | 1 | 0
        false              | 0 | 1
    }

    @Unroll
    def "Action duration depends on compute core execution for valid instruction"(){
        given:
        decoderMock.getMneumonic() >> mneumonic
        decoderMock.isValidInstruction() >> isValidInstruction
        coreMock.instrExecTime(mneumonic) >> coreExecTime

        expect:
        testObj.actionDuration() == expectedTime

        where:
        isValidInstruction | mneumonic | coreExecTime | expectedTime
        true               | _         | 5            | 5
        true               | _         | 2            | 2
        false              | _         | _            | 1
    }
}

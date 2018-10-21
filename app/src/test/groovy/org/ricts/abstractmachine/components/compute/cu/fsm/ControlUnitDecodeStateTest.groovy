package org.ricts.abstractmachine.components.compute.cu.fsm

import org.ricts.abstractmachine.components.interfaces.DecoderUnit
import org.ricts.abstractmachine.components.interfaces.FetchCore
import spock.lang.Specification

class ControlUnitDecodeStateTest extends Specification {

    ControlUnitDecodeState testObj

    DecoderUnit decoderMock = Mock()
    FetchCore fetchCoreMock = Mock()

    def setup() {
        testObj = new ControlUnitDecodeState(decoderMock, fetchCoreMock)
    }

    def "Should invoke decoder with data from fetch core when state performs action"(){
        given:
        int pc = 5
        int ir = 102
        fetchCoreMock.getInstructionPC() >> pc
        fetchCoreMock.getIR() >> ir

        when:
        testObj.performAction()

        then:
        1 * decoderMock.decode(pc, ir)
    }
}

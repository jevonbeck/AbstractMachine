package org.ricts.abstractmachine.components.compute.cu.fsm

import org.ricts.abstractmachine.components.interfaces.ComputeCore
import spock.lang.Specification

class ControlUnitSleepStateTest extends Specification {

    ControlUnitSleepState testObj

    ComputeCore coreMock = Mock()

    def setup() {
        testObj = new ControlUnitSleepState(coreMock)
    }

    def "Should only check ComputeCore interrupts in this state"(){
        when:
        testObj.performAction()
        
        then:
        1 * coreMock.checkInterrupts()
    }
}

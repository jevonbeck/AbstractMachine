package org.ricts.abstractmachine.components.compute.cu.fsm

import spock.lang.Specification
import spock.lang.Unroll

class PipelinedCuStateTest extends Specification {

    PipelinedCuState testObj

    ControlUnitFSM fsmMock1 = Mock()
    ControlUnitFSM fsmMock2 = Mock()
    ControlUnitFSM fsmMock3 = Mock()

    def setup() {
        testObj = new PipelinedCuState(ControlUnitState.GenericCUState.ACTIVE, fsmMock1, fsmMock2, fsmMock3)
    }

    def "Should trigger state change for each FSM when action performed"(){
        when:
        testObj.performAction()

        then:
        1 * fsmMock1.triggerStateChange()
        1 * fsmMock2.triggerStateChange()
        1 * fsmMock3.triggerStateChange()
    }

    @Unroll
    def "Should return max action time for next action duration"(){
        given:
        int maxValue = Math.max(Math.max(actionValue1, actionValue2), actionValue3)
        fsmMock1.nextActionDuration() >> actionValue1
        fsmMock2.nextActionDuration() >> actionValue2
        fsmMock3.nextActionDuration() >> actionValue3

        expect:
        testObj.actionDuration() == maxValue

        where:
        actionValue1 | actionValue2 | actionValue3
        1            | 2            | 3
        2            | 3            | 1
        3            | 1            | 2
    }
}

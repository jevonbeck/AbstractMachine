package org.ricts.abstractmachine.components.compute.cu.fsm

import org.ricts.abstractmachine.components.interfaces.FetchCore
import spock.lang.Specification

class ControlUnitFetchStateTest extends Specification {

    ControlUnitFetchState testObj

    FetchCore fetchCoreMock = Mock()

    def setup() {
        testObj = new ControlUnitFetchState(fetchCoreMock)
    }

    def "Should invoke fetch instruction when state performs action"(){
        when:
        testObj.performAction()

        then:
        1 * fetchCoreMock.fetchInstruction()
    }

    def "State action duration should depend on fetch core fetch time"(){
        given:
        fetchCoreMock.fetchTime() >> 5

        expect:
        testObj.actionDuration() == fetchCoreMock.fetchTime()
    }
}

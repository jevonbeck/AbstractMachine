package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.fsm.State;
import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.FetchCore;

/**
 * Created by Jevon on 25/03/2017.
 */

public class PipelinedControlUnitFSM extends CuFsmCore {
    private static final String FETCH_STATE = ControlUnitState.GenericCUState.FETCH.name();
    private static final String DECODE_STATE = ControlUnitState.GenericCUState.DECODE.name();
    private static final String EXECUTE_STATE = ControlUnitState.GenericCUState.EXECUTE.name();
    private static final String HALT_STATE = ControlUnitState.GenericCUState.HALT.name();
    private static final String SLEEP_STATE = ControlUnitState.GenericCUState.SLEEP.name();

    private ControlUnitFSM fsm1, fsm2, fsm3;
    private ControlUnitState active;

    public PipelinedControlUnitFSM(FetchCore regCore, ComputeCore core){
        fsm1 = new ControlUnitFSM(regCore, core);
        fsm2 = new ControlUnitFSM(regCore, core);
        fsm3 = new ControlUnitFSM(regCore, core);

        // setup instruction cycle
        active = new PipelinedCuState(ControlUnitState.GenericCUState.ACTIVE, fsm1, fsm2, fsm3);
        halt = new PipelinedCuState(ControlUnitState.GenericCUState.HALT, fsm1, fsm2, fsm3);
        sleep = new PipelinedCuState(ControlUnitState.GenericCUState.SLEEP, fsm1, fsm2, fsm3);
    }

    @Override
    public void setNextState(String state) {
        super.setNextState(state);
        State tempState = getNextState();

        if(tempState == sleep) {
            setNextState(HALT_STATE, HALT_STATE, SLEEP_STATE);
        }
        else if(tempState == halt) {
            setNextState(HALT_STATE, HALT_STATE, HALT_STATE);
        }
        else if(tempState == active) {
            setNextState(FETCH_STATE, DECODE_STATE, EXECUTE_STATE);
        }
    }

    @Override
    public void setCurrentState(String state) {
        super.setCurrentState(state);
        State tempState = getCurrentState();

        if(tempState == active) {
            setCurrentState(FETCH_STATE, DECODE_STATE, EXECUTE_STATE);
        }
        else if(tempState == sleep) {
            setCurrentState(HALT_STATE, HALT_STATE, SLEEP_STATE);
        }
        else if(tempState == halt) {
            setCurrentState(HALT_STATE, HALT_STATE, HALT_STATE);
        }
    }

    @Override
    public int parallelStageCount() {
        return 3;
    }

    @Override
    protected State desiredNextState(State currentState) {
        return currentState; // all states desire to stay in state unless nextState is set
    }

    @Override
    protected State convertToState(String stateName) {
        switch (Enum.valueOf(ControlUnitState.GenericCUState.class, stateName)){
            case ACTIVE:
                return active;
            case HALT:
                return halt;
            case SLEEP:
                return sleep;
            default:
                return null;
        }
    }

    @Override
    protected String initialState() {
        return active.getName();
    }

    public boolean isInActiveState(){
        return getCurrentState() == active;
    }

    public ControlUnitFSM getFsm1(){
        return fsm1;
    }

    public ControlUnitFSM getFsm2(){
        return fsm2;
    }

    public ControlUnitFSM getFsm3(){
        return fsm3;
    }

    private void setCurrentState(String fsmState1, String fsmState2, String fsmState3) {
        fsm1.setCurrentState(fsmState1);
        fsm2.setCurrentState(fsmState2);
        fsm3.setCurrentState(fsmState3);
    }

    private void setNextState(String fsmState1, String fsmState2, String fsmState3) {
        fsm1.setNextState(fsmState1);
        fsm2.setNextState(fsmState2);
        fsm3.setNextState(fsmState3);
    }
}

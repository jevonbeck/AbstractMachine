package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.fsm.State;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.FetchCore;

/**
 * Created by Jevon on 25/03/2017.
 */

public class PipelinedControlUnitAltFSM extends CuFsmCore {
    private static final String FETCH_STATE = ControlUnitState.GenericCUState.FETCH.name();
    private static final String DECODE_STATE = ControlUnitState.GenericCUState.DECODE.name();
    private static final String EXECUTE_STATE = ControlUnitState.GenericCUState.EXECUTE.name();
    private static final String HALT_STATE = ControlUnitState.GenericCUState.HALT.name();
    private static final String SLEEP_STATE = ControlUnitState.GenericCUState.SLEEP.name();

    private ControlUnitAltFSM fsm1, fsm2, fsm3;
    private ControlUnitState active;

    public PipelinedControlUnitAltFSM(FetchCore regCore, CompCore core){
        fsm1 = new ControlUnitAltFSM(regCore, core);
        fsm2 = new ControlUnitAltFSM(regCore, core);
        fsm3 = new ControlUnitAltFSM(regCore, core);

        // setup instruction cycle
        active = new PipelinedCuAltState(ControlUnitState.GenericCUState.ACTIVE, fsm1, fsm2, fsm3);
        halt = new PipelinedCuAltState(ControlUnitState.GenericCUState.HALT, fsm1, fsm2, fsm3);
        sleep = new PipelinedCuAltState(ControlUnitState.GenericCUState.SLEEP, fsm1, fsm2, fsm3);
    }

    @Override
    public void setNextState(String state) {
        super.setNextState(state);
        State tempState = getNextState();

        if(tempState == sleep) {
            setFSMSleepState();
        }
        else if(tempState == halt) {
            setFSMHaltState();
        }
        else if(tempState == active) {
            setFSMActiveState();
        }
    }

    @Override
    public void setCurrentState(String state) {
        super.setCurrentState(state);
        State tempState = getCurrentState();

        if(tempState == active) {
            setToActiveState();
        }
        else if(tempState == sleep) {
            setFSMSleepState();
        }
        else if(tempState == halt) {
            setFSMHaltState();
        }
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

    public void setToActiveState() {
        /* N.B. : Both FSMs are connected to the same instructionCache and dataMemory!
           During normal operation, one performs a fetch while the other executes... ALWAYS! */
        setCurrentState(active);
        setFSMActiveState();
    }

    public boolean isInActiveState(){
        return getCurrentState() == active;
    }

    public ControlUnitAltFSM getFsm1(){
        return fsm1;
    }

    public ControlUnitAltFSM getFsm2(){
        return fsm2;
    }

    public ControlUnitAltFSM getFsm3(){
        return fsm3;
    }

    private void setFSMSleepState() {
        fsm1.setNextState(HALT_STATE);
        fsm2.setNextState(HALT_STATE);
        fsm3.setNextState(SLEEP_STATE);
    }

    private void setFSMHaltState() {
        fsm1.setNextState(HALT_STATE);
        fsm2.setNextState(HALT_STATE);
        fsm3.setNextState(HALT_STATE);
    }

    private void setFSMActiveState() {
        fsm1.setNextState(FETCH_STATE);
        fsm2.setNextState(DECODE_STATE);
        fsm3.setNextState(EXECUTE_STATE);
    }
}

package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.fsm.State;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;

/**
 * Created by Jevon on 25/03/2017.
 */

public class PipelinedControlUnitFSM extends CuFsmCore {
    private static final String FETCH_STATE = ControlUnitState.GenericCUState.FETCH.name();
    private static final String EXECUTE_STATE = ControlUnitState.GenericCUState.EXECUTE.name();
    private static final String HALT_STATE = ControlUnitState.GenericCUState.HALT.name();
    private static final String SLEEP_STATE = ControlUnitState.GenericCUState.SLEEP.name();

    private ControlUnitFSM fsm1, fsm2;
    private ControlUnitState active;

    public PipelinedControlUnitFSM(ControlUnitRegCore regCore, ComputeCoreInterface core){
        fsm1 = new ControlUnitFSM(regCore, core);
        fsm2 = new ControlUnitFSM(regCore, core);

        // setup instruction cycle
        active = new PipelinedCuState(ControlUnitState.GenericCUState.ACTIVE, fsm1, fsm2);
        halt = new PipelinedCuState(ControlUnitState.GenericCUState.HALT, fsm1, fsm2);
        sleep = new PipelinedCuState(ControlUnitState.GenericCUState.SLEEP, fsm1, fsm2);
    }

    @Override
    public void setNextState(String state) {
        super.setNextState(state);
        State tempState = getNextState();

        if(tempState == sleep) {
            fsm1.setNextState(HALT_STATE);
            fsm2.setNextState(SLEEP_STATE);
        }
        else if(tempState == halt) {
            fsm1.setNextState(HALT_STATE);
            fsm2.setNextState(HALT_STATE);
        }
        else if(tempState == active) {
            fsm1.setNextState(FETCH_STATE);
            fsm2.setNextState(EXECUTE_STATE);
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
            fsm1.setCurrentState(HALT_STATE);
            fsm2.setCurrentState(SLEEP_STATE);
        }
        else if(tempState == halt) {
            fsm1.setCurrentState(HALT_STATE);
            fsm2.setCurrentState(HALT_STATE);
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
        fsm1.setCurrentState(FETCH_STATE);
        fsm2.setCurrentState(EXECUTE_STATE);
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
}

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
            setFSMSleepNextState();
        }
        else if(tempState == halt) {
            setFSMHaltNextState();
        }
        else if(tempState == active) {
            setFSMActiveNextState();
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
            setToSleepState();
        }
        else if(tempState == halt) {
            setToHaltState();
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

    private void setFSMSleepNextState() {
        fsm1.setNextState(HALT_STATE);
        fsm2.setNextState(HALT_STATE);
        fsm3.setNextState(SLEEP_STATE);
    }

    private void setFSMHaltNextState() {
        fsm1.setNextState(HALT_STATE);
        fsm2.setNextState(HALT_STATE);
        fsm3.setNextState(HALT_STATE);
    }

    private void setFSMActiveNextState() {
        fsm1.setNextState(FETCH_STATE);
        fsm2.setNextState(DECODE_STATE);
        fsm3.setNextState(EXECUTE_STATE);
    }

    private void setToActiveState() {
        /* N.B. : All FSMs are connected to the same instructionCache and dataMemory!
           During normal operation, each performs a different execution state... ALWAYS! */
        fsm1.setCurrentState(FETCH_STATE);
        fsm2.setCurrentState(DECODE_STATE);
        fsm3.setCurrentState(EXECUTE_STATE);
    }

    private void setToSleepState() {
        fsm1.setCurrentState(HALT_STATE);
        fsm2.setCurrentState(HALT_STATE);
        fsm3.setCurrentState(SLEEP_STATE);
    }

    private void setToHaltState() {
        fsm1.setCurrentState(HALT_STATE);
        fsm2.setCurrentState(HALT_STATE);
        fsm3.setCurrentState(HALT_STATE);
    }
}

package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.fsm.State;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.FetchCore;

/**
 * Created by Jevon on 26/08/2016.
 */
public class ControlUnitFSM extends CuFsmCore {
    private ControlUnitState fetch, execute;

    public ControlUnitFSM(FetchCore regCore, ComputeCoreInterface core){
        // setup instruction cycle
        fetch = new ControlUnitFetchState(regCore);
        execute = new ControlUnitExecuteState(core, regCore);
        halt = new ControlUnitHaltState();
        sleep = new ControlUnitSleepState(core);
    }

    @Override
    public int parallelStageCount() {
        return 1;
    }

    @Override
    protected State desiredNextState(State currentState) {
        if(currentState == fetch)
            return execute;
        else if(currentState == execute)
            return fetch;
        else if(currentState == sleep)
            return sleep;

        return halt;
    }

    @Override
    protected State convertToState(String stateName){
        switch (Enum.valueOf(ControlUnitState.GenericCUState.class, stateName)){
            case FETCH:
                return fetch;
            case EXECUTE:
                return execute;
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
        return fetch.getName();
    }

    public boolean isInFetchState() {
        return getCurrentState() == fetch;
    }

    public boolean isInExecuteState(){
        return getCurrentState() == execute;
    }
}

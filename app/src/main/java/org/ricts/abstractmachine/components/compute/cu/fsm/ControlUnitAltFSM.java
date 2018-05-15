package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.fsm.State;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.interfaces.FetchCore;

/**
 * Created by Jevon on 26/08/2016.
 */
public class ControlUnitAltFSM extends CuFsmCore {
    private ControlUnitState fetch, decode, execute;

    public ControlUnitAltFSM(FetchCore regCore, CompCore core){
        DecoderUnit decoder = core.getDecoderUnit();

        // setup instruction cycle
        fetch = new ControlUnitFetchState(regCore);
        decode = new ControlUnitDecodeState(decoder, regCore);
        execute = new ControlUnitComputeState(core, decoder);
        halt = new ControlUnitHaltState();
        sleep = new ControlUnitAltSleepState(core);
    }

    @Override
    public int parallelStageCount() {
        return 1;
    }

    @Override
    protected State desiredNextState(State currentState) {
        if(currentState == fetch)
            return decode;
        else if(currentState == decode)
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
            case DECODE:
                return decode;
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

    public boolean isInDecodeState() {
        return getCurrentState() == decode;
    }

    public boolean isInExecuteState(){
        return getCurrentState() == execute;
    }
}

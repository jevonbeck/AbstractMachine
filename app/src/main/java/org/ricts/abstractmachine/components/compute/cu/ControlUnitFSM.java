package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuInternalInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

/**
 * Created by Jevon on 26/08/2016.
 */
public class ControlUnitFSM extends FiniteStateMachine {

    private ControlUnitState fetch, execute, halt, sleep;
    private State nextState = null;

    public ControlUnitFSM(CuInternalInterface cu, ComputeCoreInterface core, ReadPort instructionCache){
        // setup instruction cycle
        fetch = new ControlUnitFetchState(cu, instructionCache);
        execute = new ControlUnitExecuteState(core, cu);
        halt = new ControlUnitHaltState();
        sleep = new ControlUnitSleepState(core);
    }

    @Override
    protected State getNextState(State currentState) {
        if(nextState != null){ // If normal cycle interrupted ...
            // ... next state determined by interrupting state
            currentState = nextState;
            nextState = null;
            return currentState;
        }

        // Otherwise next state determined by current state
        if(currentState == fetch)
            return execute;
        else if(currentState == execute)
            return fetch;
        else if(currentState == sleep)
            return sleep;

        return halt;
    }

    public void setNextStateToHalt() {
        nextState = halt;
    }

    public void setNextStateToSleep() {
        nextState = sleep;
    }

    public void setNextStateToFetch() {
        nextState = fetch;
    }

    public void setToFetchState(){
        setCurrentState(fetch);
    }

    public void setToExecuteState(){
        setCurrentState(execute);
    }

    public boolean isInFetchState() {
        return currentState() == fetch;
    }

    public boolean isInExecuteState(){
        return currentState() == execute;
    }

    public boolean isInHaltState(){
        return currentState() == halt;
    }

    public boolean isInSleepState(){
        return currentState() == sleep;
    }

    public int nextActionDuration(){ // in clock cycles
        return ((ControlUnitState) currentState()).actionDuration();
    }

    public String getCurrentStateString(){
        return currentState().getName();
    }
}

package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.fsm.FiniteStateMachine;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;

/**
 * Created by Jevon on 27/03/2017.
 */

public abstract class CuFsmCore extends FiniteStateMachine implements CuFsmInterface {
    protected ControlUnitState halt, sleep;

    @Override
    public boolean isInHaltState(){
        return getCurrentState() == halt;
    }

    @Override
    public boolean isInSleepState(){
        return getCurrentState() == sleep;
    }

    @Override
    public int nextActionDuration(){ // in clock cycles
        return ((ControlUnitState) getCurrentState()).actionDuration();
    }
}

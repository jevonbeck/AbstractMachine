package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;

/**
 * Created by Jevon on 14/08/2016.
 */
public abstract class CpuCore implements ThreadProcessingUnit {
    public abstract ObservableControlUnit getControlUnit();
    protected abstract CuDataInterface createControlUnit(ComputeCoreInterface core, ReadPort instructionCache);

    @Override
    public void setStartExecFrom(int currentPC) {
        getControlUnit().setStartExecFrom(currentPC);
    }

    @Override
    public int nextActionTransitionTime(){
        return getControlUnit().nextActionDuration();
    }

    @Override
    public void triggerNextAction(){
        getControlUnit().performNextAction(); // perform action for 'currentState' and go to next state
    }

    @Override
    public void reset() {
        getControlUnit().reset();
    }
}

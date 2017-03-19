package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;

/**
 * Created by Jevon on 14/08/2016.
 */
public abstract class CpuCore implements ThreadProcessingUnit {
    private ObservableControlUnit cu; // Control Unit

    protected abstract ControlUnitCore createControlUnit(ComputeCoreInterface core, ReadPort instructionCache);

    @Override
    public void setStartExecFrom(int currentPC) {
        cu.setStartExecFrom(currentPC);
    }

    @Override
    public int nextActionTransitionTime(){
        return cu.nextActionDuration();
    }

    @Override
    public void triggerNextAction(){
        cu.performNextAction(); // perform action for 'currentState' and go to next state
    }

    @Override
    public void reset() {
        cu.reset();
    }

    public ObservableControlUnit getControlUnit(){
        return cu;
    }

    protected void createObservableControlUnit(ComputeCoreInterface core, ReadPort instructionCache) {
        cu = new ObservableControlUnit(createControlUnit(core, instructionCache));
        core.setControlUnit(cu);
    }
}

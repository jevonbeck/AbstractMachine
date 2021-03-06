package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;

/**
 * Created by Jevon on 14/08/2016.
 */
public abstract class CpuCore implements ThreadProcessingUnit {
    private ControlUnitCore cu; // Control Unit

    protected abstract ControlUnitCore createControlUnit(ComputeCore core, ReadPort instructionCache);

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

    public ControlUnitCore getControlUnit(){
        return cu;
    }

    protected void initCpuCore(ComputeCore core, ReadPort instructionCache) {
        cu = createControlUnit(core, instructionCache);
        core.setControlUnit(cu);
    }
}

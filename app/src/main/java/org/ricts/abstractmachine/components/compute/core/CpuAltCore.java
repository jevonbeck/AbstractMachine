package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;

/**
 * Created by Jevon on 14/08/2016.
 */
public abstract class CpuAltCore implements ThreadProcessingUnit {
    private ControlUnitAltCore cu; // Control Unit

    protected abstract ControlUnitAltCore createControlUnit(CompCore core, ReadPort instructionCache);

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

    public ControlUnitAltCore getControlUnit(){
        return cu;
    }

    protected void initCpuCore(CompCore core, ReadPort instructionCache) {
        cu = createControlUnit(core, instructionCache);
        core.setControlUnit(cu);
    }
}

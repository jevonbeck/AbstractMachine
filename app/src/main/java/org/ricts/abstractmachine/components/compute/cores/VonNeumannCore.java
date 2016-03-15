package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;

public class VonNeumannCore implements ThreadProcessingUnit{
    private ObservableControlUnit cu; // Control Unit

    public VonNeumannCore(ComputeCoreInterface core, MemoryPort dataMemory){
        cu = new ObservableControlUnit(new ControlUnit(core, dataMemory, dataMemory));
    }

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

    public ObservableControlUnit getControlUnit(){
        return cu;
    }
}
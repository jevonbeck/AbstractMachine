package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.storage.Register;

public class VonNeumannCore implements ThreadProcessingUnit{
    private ControlUnitInterface cu; // Control Unit

    public VonNeumannCore(ComputeCoreInterface core, MemoryPort dataMemory){
        //Register pc = new Register(core.iAddrWidth()); // Program Counter
        //Register ir = new Register(core.instrWidth()); // Instruction Register
        cu = new ControlUnit(core, dataMemory, dataMemory);

        setStartExecFrom(0);
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
}
package org.ricts.abstractmachine.components.compute;

import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.storage.Register;

public class CpuCore implements ThreadProcessingUnit{
    private Register pc; // Program Counter
    private ControlUnit cu; // Control Unit

    public CpuCore(ComputeCore core, ReadPort instructionCache, MemoryPort dataMemory){
        pc = new Register(core.iAddrWidth());
        Register ir = new Register(core.instrWidth()); // Instruction Register
        cu = new ControlUnit(pc, ir, core, instructionCache, dataMemory);

        setStartExecFrom(0);
    }

    @Override
    public void setStartExecFrom(int currentPC){
        pc.write(currentPC);
        cu.setToFetchState();
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
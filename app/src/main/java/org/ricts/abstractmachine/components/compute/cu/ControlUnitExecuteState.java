package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;

public class ControlUnitExecuteState extends ControlUnitState {
    private ControlUnitInterface cu;
    private MemoryPort dataMemory;
    private ComputeCoreInterface core;

    public ControlUnitExecuteState(ComputeCoreInterface proc,
                                   MemoryPort dMemory, ControlUnitInterface controlUnit){
        super("execute");
        core = proc;
        dataMemory = dMemory;
        cu = controlUnit;
    }

    @Override
    public void performAction(){
        core.executeInstruction(cu.getIR(), dataMemory, cu);
    }

    @Override
    public int actionDuration(){
        return core.instrExecTime(cu.getIR(), dataMemory);
    }
}

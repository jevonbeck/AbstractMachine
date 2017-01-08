package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuInternalInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;

public class ControlUnitExecuteState extends ControlUnitState {
    private CuInternalInterface cu;
    private MemoryPort dataMemory;
    private ComputeCoreInterface core;

    public ControlUnitExecuteState(ComputeCoreInterface proc,
                                   MemoryPort dMemory, CuInternalInterface controlUnit){
        super(GenericCUState.EXECUTE);
        core = proc;
        dataMemory = dMemory;
        cu = controlUnit;
    }

    @Override
    public void performAction(){
        core.executeInstruction(cu.getPC(), cu.getIR(), dataMemory, cu);
    }

    @Override
    public int actionDuration(){
        return core.instrExecTime(cu.getIR(), dataMemory);
    }
}

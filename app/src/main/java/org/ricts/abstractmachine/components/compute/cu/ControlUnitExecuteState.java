package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnitExecuteState extends ControlUnitState {
    private ControlUnitInterface cu;
    private RegisterPort IR; // Instruction Register
    private MemoryPort dataMemory;
    private ComputeCoreInterface core;

    public ControlUnitExecuteState(RegisterPort currentInstr, ComputeCoreInterface proc,
                                   MemoryPort dMemory, ControlUnitInterface controlUnit){
        super("execute");
        IR = currentInstr;
        core = proc;
        dataMemory = dMemory;
        cu = controlUnit;
    }

    @Override
    public void performAction(){
        core.executeInstruction(IR.read(), dataMemory, cu);
    }

    @Override
    public int actionDuration(){
        return core.instrExecTime(IR.read(), dataMemory);
    }
}

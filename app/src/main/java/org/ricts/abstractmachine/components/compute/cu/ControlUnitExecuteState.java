package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnitExecuteState extends ControlUnitState {
    private RegisterPort PC; // Program Counter
    private RegisterPort IR; // Instruction Register
    private MemoryPort dataMemory;
    private ComputeCore core;

    public ControlUnitExecuteState(RegisterPort currentInstr, ComputeCore proc,
                                   MemoryPort dMemory, RegisterPort nextInstrAddr){
        super("execute");
        IR = currentInstr;
        core = proc;
        dataMemory = dMemory;
        PC = nextInstrAddr;
    }

    @Override
    public void performAction(){
        core.executeInstruction(IR.read(), dataMemory, PC);
    }

    @Override
    public int actionDuration(){
        return core.instrExecTime(IR.read(), dataMemory);
    }
}

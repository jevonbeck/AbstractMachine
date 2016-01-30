package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnitFetchState extends ControlUnitState{
    private ReadPort instructionCache;
    private RegisterPort pc; // Program Counter
    private RegisterPort ir; // Instruction Register

    public ControlUnitFetchState(RegisterPort instrAddr, RegisterPort instr, ReadPort iCache){
        super("fetch");
        pc = instrAddr;
        ir = instr;
        instructionCache = iCache;
    }

    @Override
    public void performAction(){
        ir.write(instructionCache.read(pc.read())); // IR = iCache[PC]
        pc.write(pc.read() + 1); // PC += 1
    }

    @Override
    public int actionDuration(){
        return instructionCache.accessTime();
    }
}

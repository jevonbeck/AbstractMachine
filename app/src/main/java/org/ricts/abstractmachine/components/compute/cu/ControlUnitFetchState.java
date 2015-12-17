package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnitFetchState extends ControlUnitState{
    private RegisterPort PC; // Program Counter
    private RegisterPort IR; // Instruction Register
    private ReadPort instructionCache;

    public ControlUnitFetchState(RegisterPort nextInstrAddr, ReadPort iCache, RegisterPort currentInstr){
        super("fetch");
        PC = nextInstrAddr;
        instructionCache = iCache;
        IR = currentInstr;
    }

    @Override
    public void performAction(){
        IR.write(instructionCache.read(PC.read())); // IR = iCache[PC]
        PC.write(PC.read() + 1); // PC += 1
    }

    @Override
    public int actionDuration(){
        return instructionCache.accessTime();
    }
}

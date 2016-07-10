package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ControlUnitFetchState extends ControlUnitState{
    private ReadPort instructionCache;
    private ControlUnit controlUnit;

    public ControlUnitFetchState(ControlUnit cu, ReadPort iCache){
        super("fetch");
        controlUnit = cu;
        instructionCache = iCache;
    }

    @Override
    public void performAction(){
        controlUnit.fetchInstruction(instructionCache);
    }

    @Override
    public int actionDuration(){
        return instructionCache.accessTime();
    }
}

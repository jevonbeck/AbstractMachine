package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.CuInternalInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ControlUnitFetchState extends ControlUnitState{
    private ReadPort instructionCache;
    private CuInternalInterface controlUnit;

    public ControlUnitFetchState(CuInternalInterface cu, ReadPort iCache){
        super(GenericCUState.FETCH);
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

package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnitFetchState extends ControlUnitState{
    private ReadPort instructionCache;
    private ControlUnitInterface controlUnit;

    public ControlUnitFetchState(ControlUnitInterface cu, ReadPort iCache){
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

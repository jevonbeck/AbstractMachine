package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;

public class ControlUnitFetchState extends ControlUnitState {
    private ControlUnitRegCore regCore;

    public ControlUnitFetchState(ControlUnitRegCore cu){
        super(GenericCUState.FETCH);
        regCore = cu;
    }

    @Override
    public void performAction(){
        regCore.fetchInstruction();
    }

    @Override
    public int actionDuration(){
        return regCore.fetchTime();
    }
}

package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.FetchCore;

public class ControlUnitFetchState extends ControlUnitState {
    private FetchCore fetchCore;

    public ControlUnitFetchState(FetchCore cu){
        super(GenericCUState.FETCH);
        fetchCore = cu;
    }

    @Override
    public void performAction(){
        fetchCore.fetchInstruction();
    }

    @Override
    public int actionDuration(){
        return fetchCore.fetchTime();
    }
}

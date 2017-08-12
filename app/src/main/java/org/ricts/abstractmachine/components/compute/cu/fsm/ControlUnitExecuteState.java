package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.FetchCore;

public class ControlUnitExecuteState extends ControlUnitState {
    private FetchCore cuRegCore;
    private ComputeCoreInterface core;

    public ControlUnitExecuteState(ComputeCoreInterface proc, FetchCore fetchCore){
        super(GenericCUState.EXECUTE);
        core = proc;
        cuRegCore = fetchCore;
    }

    @Override
    public void performAction(){
        core.executeInstruction(cuRegCore.getPC(), cuRegCore.getIR());
    }

    @Override
    public int actionDuration(){
        return core.instrExecTime(cuRegCore.getIR());
    }
}

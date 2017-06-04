package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;

public class ControlUnitExecuteState extends ControlUnitState {
    private ControlUnitRegCore cuRegCore;
    private ComputeCoreInterface core;

    public ControlUnitExecuteState(ComputeCoreInterface proc, ControlUnitRegCore regCore){
        super(GenericCUState.EXECUTE);
        core = proc;
        cuRegCore = regCore;
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

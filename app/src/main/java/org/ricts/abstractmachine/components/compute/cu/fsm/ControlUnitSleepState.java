package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.ComputeCore;

/**
 * Created by Jevon on 08/08/2016.
 */
public class ControlUnitSleepState extends ControlUnitState {
    private ComputeCore core;

    public ControlUnitSleepState(ComputeCore proc) {
        super(GenericCUState.SLEEP);
        core = proc;
    }

    @Override
    public int actionDuration() {
        return 1;
    }

    @Override
    public void performAction() {
        core.checkInterrupts();
    }
}

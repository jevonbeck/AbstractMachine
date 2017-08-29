package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.CompCore;

/**
 * Created by Jevon on 08/08/2016.
 */
public class ControlUnitAltSleepState extends ControlUnitState {
    private CompCore core;

    public ControlUnitAltSleepState(CompCore proc) {
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

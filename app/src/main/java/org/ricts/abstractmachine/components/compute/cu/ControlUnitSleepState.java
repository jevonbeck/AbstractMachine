package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;

/**
 * Created by Jevon on 08/08/2016.
 */
public class ControlUnitSleepState extends ControlUnitState{
    private ComputeCoreInterface core;
    private ControlUnitInterface cu;

    public ControlUnitSleepState(ComputeCoreInterface proc, ControlUnitInterface controlUnit) {
        super(GenericCUState.SLEEP);
        core = proc;
        cu = controlUnit;
    }

    @Override
    public int actionDuration() {
        return 1;
    }

    @Override
    public void performAction() {
        core.checkInterrupts(cu);
    }
}

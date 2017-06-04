package org.ricts.abstractmachine.components.compute.cu.fsm;

/**
 * Created by Jevon on 17/12/2015.
 */
public class ControlUnitHaltState extends ControlUnitState {
    public ControlUnitHaltState() {
        super(GenericCUState.HALT);
    }

    @Override
    public int actionDuration() {
        return 1;
    }

    @Override
    public void performAction() {
        // Do nothing! This state is made for this purpose.
    }
}

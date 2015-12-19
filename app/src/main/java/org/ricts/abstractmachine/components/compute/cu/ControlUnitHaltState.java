package org.ricts.abstractmachine.components.compute.cu;

/**
 * Created by Jevon on 17/12/2015.
 */
public class ControlUnitHaltState extends ControlUnitState{
    public ControlUnitHaltState() {
        super("halt");
        setNextState(this);
    }

    @Override
    public int actionDuration() {
        return 1;
    }

    @Override
    public void performAction() {
        // TODO: Do nothing for now! Implement appropriate logic when interrupts are implemented
    }
}

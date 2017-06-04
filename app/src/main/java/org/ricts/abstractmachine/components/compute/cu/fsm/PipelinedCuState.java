package org.ricts.abstractmachine.components.compute.cu.fsm;


/**
 * Created by Jevon on 26/03/2017.
 */

public class PipelinedCuState extends ControlUnitState{
    private ControlUnitFSM fsm1, fsm2;

    public PipelinedCuState(GenericCUState sName, ControlUnitFSM sm1, ControlUnitFSM sm2) {
        super(sName);
        fsm1 = sm1;
        fsm2 = sm2;
    }

    @Override
    public void performAction() {
        // advance both FSMs
        fsm1.triggerStateChange();
        fsm2.triggerStateChange();
    }

    @Override
    public int actionDuration() {
        return Math.max(fsm1.nextActionDuration(), fsm2.nextActionDuration());
    }
}

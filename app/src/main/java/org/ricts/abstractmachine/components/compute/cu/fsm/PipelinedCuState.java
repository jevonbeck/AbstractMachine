package org.ricts.abstractmachine.components.compute.cu.fsm;


/**
 * Created by Jevon on 26/03/2017.
 */

public class PipelinedCuState extends ControlUnitState{
    private ControlUnitFSM[] fsms;

    public PipelinedCuState(GenericCUState sName, ControlUnitFSM... sms) {
        super(sName);
        fsms = sms;
    }

    @Override
    public void performAction() {
        // advance FSMs
        for(ControlUnitFSM fsm : fsms) {
            fsm.triggerStateChange();
        }
    }

    @Override
    public int actionDuration() {
        int max = 0;
        for(ControlUnitFSM fsm : fsms) {
            max = Math.max(max, fsm.nextActionDuration());
        }
        return max;
    }
}

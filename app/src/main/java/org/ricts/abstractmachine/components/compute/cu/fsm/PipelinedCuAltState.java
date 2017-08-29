package org.ricts.abstractmachine.components.compute.cu.fsm;


/**
 * Created by Jevon on 26/03/2017.
 */

public class PipelinedCuAltState extends ControlUnitState{
    private ControlUnitAltFSM[] fsms;

    public PipelinedCuAltState(GenericCUState sName, ControlUnitAltFSM... sms) {
        super(sName);
        fsms = sms;
    }

    @Override
    public void performAction() {
        // advance FSMs
        for(ControlUnitAltFSM fsm : fsms) {
            fsm.triggerStateChange();
        }
    }

    @Override
    public int actionDuration() {
        int max = 0;
        for(ControlUnitAltFSM fsm : fsms) {
            max = Math.max(max, fsm.nextActionDuration());
        }
        return max;
    }
}
